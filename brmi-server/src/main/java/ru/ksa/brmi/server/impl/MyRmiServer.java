package ru.ksa.brmi.server.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import ru.ksa.brmi.exception.MyRmiBaseException;

public class MyRmiServer {
	static final Logger log = org.slf4j.LoggerFactory.getLogger(MyRmiServer.class);

	private final int portNumber;

	private final ExecutorService sessionExecutor = Executors.newCachedThreadPool();

	public MyRmiServer(int portNumber) {
		this.portNumber = portNumber;
	}

	public MyRmiServer loadConfiguration(Properties prop) {
		prop.forEach((key, value) -> {
			try {
				MyRmiServiceLocator.INSTANCE.registerService((String) key, (String) value);
			} catch (MyRmiBaseException e) {
				log.error("Server configuration error {}", e.getMessage());
			}
		});
		return this;
	}

	public MyRmiServer start() {

		try (ServerSocket server = new ServerSocket(portNumber);
				BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {

			while (server.isBound() & !server.isClosed()) {
				try {
					if (consoleReader.ready()) {
						String consoleCommand = consoleReader.readLine();
						if (consoleCommand.equalsIgnoreCase("shutdown")) {
							log.info("Server initiate shutdown ...");
							server.close();
							break;
						}
					}
					log.info("Wait client connection accepted.");
					Socket client = server.accept();
					sessionExecutor.submit(new MyRmiServerConnection(client));
					log.debug("Server submit a new connection on host {} local port {}", client.getInetAddress(),
							client.getPort());
				} catch (Exception e) {
					log.error("Server intenal error", e);
				}
			}
		} catch (IOException e) {
			log.error("Error starting server ", e);
		}
		sessionExecutor.shutdown();
		try {
			sessionExecutor.awaitTermination(1L, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			log.error("Server error stoping executors", e);
		}
		log.info("Server stopped.");
		return this;
	}

}
