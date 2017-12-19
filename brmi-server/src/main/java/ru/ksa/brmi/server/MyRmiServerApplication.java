package ru.ksa.brmi.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.ksa.brmi.server.impl.MyRmiServer;

public class MyRmiServerApplication {
	static final Logger log = LoggerFactory.getLogger(MyRmiServerApplication.class);
	static final String FILE_NAME = "/server.properties";

	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("Ussage java -cp MyRmiServerApplication <port number>");
			return;
		}
		int portNumber = Integer.parseInt(args[0]);
		MyRmiServer server = new MyRmiServer(portNumber);
		try {
			server.loadConfiguration(loadProperties(FILE_NAME)).start();
		} catch (IOException e) {
			log.error("Can't start server.", e);
		}
	}

	static Properties loadProperties(String fileName) throws IOException {
		Objects.requireNonNull(fileName);
		Properties result = new Properties();
		try (InputStream is = MyRmiServer.class.getResourceAsStream(fileName);) {
			if (is == null)
				throw new FileNotFoundException(fileName);
			result.load(is);
		} catch (IOException ex) {
			log.error("Can't read property file " + fileName, ex);
			throw ex;
		}
		return result;
	}
}
