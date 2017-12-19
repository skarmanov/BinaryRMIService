package ru.ksa.brmi.client;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.ksa.brmi.api.MyRmiRequest;
import ru.ksa.brmi.api.MyRmiResponse;
import ru.ksa.brmi.api.MyRmiResult;
import ru.ksa.brmi.exception.MyRmiBaseException;

public class MyRmiClient implements Closeable {
	private static final long DEFAULT_TIMEOUT = 5;
	private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;

	private static final Logger log = LoggerFactory.getLogger(MyRmiClient.class);

	private final ReentrantLock initLock = new ReentrantLock();
	private final Semaphore outSemaphore = new Semaphore(1);
	private final Semaphore inSemaphore = new Semaphore(1);

	private final AtomicInteger requestNo = new AtomicInteger(0);
	private final AtomicBoolean isShutdown = new AtomicBoolean(false);

	private final ResponseReceiver responseReceiver = new ResponseReceiver();

	private final ConcurrentMap<Integer, Exchanger<MyRmiResponse>> waitRequestMap = new ConcurrentHashMap<>();

	private final String host;
	private final int port;

	private volatile Socket socket = null;
	private volatile ObjectOutputStream socketOutputStream = null;
	private volatile ObjectInputStream socketInputStream = null;

	public MyRmiClient(String host, int port) {
		super();
		this.host = host;
		this.port = port;
	}

	/**
	 * Lazy client initialization If socked not created, create socket and
	 * initialize socket output stream and input stream.
	 * 
	 * @throws IOException
	 */
	private void initClient() throws IOException {
		if (socket == null) {
			initLock.lock();
			try {
				if (socket == null) {
					Socket tmpSocket = new Socket(host, port);
					log.info("Client connected to socket.");
					socketOutputStream = new ObjectOutputStream(tmpSocket.getOutputStream());
					socketInputStream = new ObjectInputStream(tmpSocket.getInputStream());
					socket = tmpSocket;
					Thread threadReceiver = new Thread(responseReceiver);
					threadReceiver.setName("Response Receiver");
					threadReceiver.start();
				}
			} finally {
				initLock.unlock();
			}
		}
	}

	public MyRmiResult<?> remoteCall(String serviceName, String method, Object[] params) throws MyRmiBaseException {
		MyRmiResponse response = null;
		try {
			initClient();
			MyRmiRequest request = new MyRmiRequest(requestNo.incrementAndGet(), serviceName, method, params);
			Exchanger<MyRmiResponse> exchanger = sendRequest(request);
			if (exchanger == null)
				return null;
			response = exchanger.exchange(null, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT);
			log.info("{}:{} receive response from server: {} by request {}", socket.getInetAddress(),
					socket.getLocalPort(), response, request);
		} catch (IOException | InterruptedException | TimeoutException e) {
			log.error("Error client remoteCall", e);
			throw new MyRmiBaseException(e);
		}
		if (response.getStatus() == MyRmiResponse.StatusType.OK) {
			return response.getResult();
		} else
			throw response.getException();
	}

	/**
	 * Send a request to the server and return a synchronize object Exchanger
	 * 
	 * @param request
	 *            client request
	 * @return Exchanger or null if can't acquire output stream semaphore or
	 *         error write to the socket output stream
	 * @throws IOException
	 */
	private Exchanger<MyRmiResponse> sendRequest(MyRmiRequest request) throws IOException {
		initClient();
		Exchanger<MyRmiResponse> result = new Exchanger<>();
		// Register request before send to server, when receive response before
		// put
		waitRequestMap.put(request.getRequestNo(), result);
		try {
			try {
				outSemaphore.acquire();
				socketOutputStream.writeObject(request);
				socketOutputStream.flush();
			} finally {
				outSemaphore.release();
			}
			log.info("{}:{} send request {}", socket.getInetAddress(), socket.getLocalPort(), request);
		} catch (InterruptedException e) {
			waitRequestMap.remove(request.getRequestNo());
			throw new IOException(e);
		} catch (IOException e) {
			// Remove request when exception send request
			waitRequestMap.remove(request.getRequestNo());
			throw e;
		}
		return result;
	}

	/**
	 * Read a response from the server and return the response object
	 * 
	 * @return MyRmiResponse
	 * @throws IOException,
	 *             ClassNotFoundException if can't acquire input stream
	 *             semaphore or error read from the socket input stream
	 */
	private MyRmiResponse readResponse() throws IOException, ClassNotFoundException {
		initClient();
		MyRmiResponse result = null;
		try {
			inSemaphore.acquire();
			if (!socket.isClosed() & !socket.isInputShutdown()) {
				result = (MyRmiResponse) socketInputStream.readObject();
			}
		} catch (InterruptedException e) {
			throw new IOException(e);
		} finally {
			inSemaphore.release();
		}
		return result;
	}

	@Override
	public void close() throws IOException {
		try (Socket s = socket; OutputStream o = socketOutputStream; InputStream i = socketInputStream;) {
		}
	}

	/**
	 * Receives a response from the server and sends it to the stream to the
	 * recipient via Exchanger
	 * 
	 * @author ksa
	 *
	 */
	class ResponseReceiver implements Runnable {
		@Override
		public void run() {
			while (!isShutdown.get()) {
				try {
					MyRmiResponse response = readResponse();
					if (response != null) {
						log.debug("Get response '{}' from server.", response);
						Exchanger<MyRmiResponse> exchanger = waitRequestMap.remove(response.getRequestNo());
						if (exchanger == null)
							log.error("Request not fond for response no {}", response.getRequestNo());
						else {
							log.debug("Response receiver start put response to client: {}", response);
							exchanger.exchange(response);
						}
					}
				} catch (EOFException e) {
					log.error("Shutdown client with EOF input stream:", e);
					isShutdown.set(true);
				} catch (IOException e) {
					log.error("Response receiver can't read data from socket input stream:", e);
				} catch (InterruptedException e) {
					log.error("Response receiver exchange interrupted.", e);
				} catch (ClassNotFoundException e) {
					log.error("Can't reading data from server:", e);
				}
			}
			StringBuffer sb = new StringBuffer();
			waitRequestMap.forEach((key, val) -> {
				sb.append("Request # " + key + ",");
			});
			log.info("Client {}:{} The requests : {} not have server response", socket.getInetAddress(),
					socket.getLocalPort(), sb.toString());
		}

	}

}
