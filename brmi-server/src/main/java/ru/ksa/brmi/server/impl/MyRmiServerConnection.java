package ru.ksa.brmi.server.impl;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.ksa.brmi.api.MyRmiRequest;
import ru.ksa.brmi.api.MyRmiResponse;
import ru.ksa.brmi.api.MyRmiResult;
import ru.ksa.brmi.exception.MyRmiBaseException;

public class MyRmiServerConnection implements Runnable {
	static final Logger log = LoggerFactory.getLogger(MyRmiServerConnection.class);

	private final Socket socket;
	private final AtomicBoolean isShutdown = new AtomicBoolean(false);
	private final ExecutorService serviceExecutor = Executors.newCachedThreadPool();
	private final CompletionService<MyRmiResponse> completionService = new ExecutorCompletionService<>(serviceExecutor);
	private final ResponseSender responseSender = new ResponseSender();

	MyRmiServerConnection(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		try (ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());) {
			Thread responseThread = new Thread(responseSender, "response-senders:" + socket.getLocalPort());
			responseThread.start();
			while (!isShutdown.get()) {
				try {
					if (!socket.isClosed() & !socket.isInputShutdown()) {
						MyRmiRequest request = (MyRmiRequest) inStream.readObject();
						log.info("{}:{} Receive request from client {} ", socket.getInetAddress(),
								socket.getLocalPort(), request);
						completionService.submit(new RequestProcessor(request));
					} else
						isShutdown.set(true);
				} catch (SocketException se) {
					log.error("SocketException: ", se);
					isShutdown.set(true);
				} catch (EOFException eof) {
					log.error("Client abort session.", eof);
					isShutdown.set(true);
				} catch (ClassNotFoundException | IOException e) {
					log.error("Error reading data from client .", e);
				}
			}
		} catch (IOException e) {
			log.error("Can't open socket input stream:", e);
			isShutdown.set(true);
		} finally {
			log.info("Shutdown the server session");
			try (Socket s = socket) {
				log.info("Close a server socket");
			} catch (IOException e) {
			}
			serviceExecutor.shutdown();
			try {
				serviceExecutor.awaitTermination(1L, TimeUnit.MINUTES);
				log.error("Server connection is stoped");
			} catch (InterruptedException e) {
				log.error("Server connection error stoping executors", e);
			}
		}
	}

	/**
	 * Read completed requests from the completion service and sends response to
	 * the client via socket output stream
	 * 
	 */
	protected class ResponseSender implements Runnable {
		@Override
		public void run() {
			try (ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());) {
				while (!isShutdown.get()) {
					Future<MyRmiResponse> future = null;
					try {
						future = completionService.poll(10, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						log.info("Response sender time out pool a future from completion service.");
					}
					if (future != null) {
						MyRmiResponse response = null;
						try {
							response = future.get();
						} catch (ExecutionException | InterruptedException ee) {
							log.error("Response sender can't get service execution result.", ee);
						}
						if (response != null) {
							try {
								if (!socket.isClosed() & !socket.isOutputShutdown()) {
									outStream.writeObject(response);
									outStream.flush();
									log.info("{}:{} Send response to client: {}", socket.getInetAddress(),
											socket.getLocalPort(), response);
								} else
									isShutdown.set(true);
							} catch (EOFException ioe) {
								log.error("Response sender can't write execution result to client.");
								isShutdown.set(true);
							}
						}
					}
				}
			} catch (IOException e) {
				log.error("Response sender can't open socket output stream:", e);
			} finally {
				isShutdown.set(true);
			}
		};

	}

	/**
	 * Execute client requests via service
	 * 
	 */
	public static class RequestProcessor implements Callable<MyRmiResponse> {
		private static final Logger log = LoggerFactory.getLogger(RequestProcessor.class);
		private final MyRmiRequest request;

		/**
		 * @param request
		 *            service request
		 */
		public RequestProcessor(MyRmiRequest request) {
			this.request = request;
		}

		@Override
		public MyRmiResponse call() throws MyRmiBaseException {
			log.debug("Start processing request {}", request);
			MyRmiResult<?> serviceResult = null;
			try {
				serviceResult = MyRmiServiceLocator.invokeService(request.getServiceName(), request.getMethod(),
						request.getParams());
				log.debug("Finish processing request: {} result: {}", request, serviceResult);
			} catch (MyRmiBaseException e) {
				return MyRmiResponse.ERROR(request.getRequestNo(), e).build();
			}
			return MyRmiResponse.OK(request.getRequestNo()).result(serviceResult).build();
		}

	}

}
