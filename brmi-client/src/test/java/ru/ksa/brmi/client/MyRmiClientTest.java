/**
 * 
 */
package ru.ksa.brmi.client;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.ksa.brmi.exception.MyRmiBaseException;

/**
 * @author ksa
 *
 */
public class MyRmiClientTest {
	private static final int THREAD_COUNT = 10;
	Thread[] callerThread = new Thread[THREAD_COUNT];

	private static class Caller implements Runnable {
		private Logger log = LoggerFactory.getLogger(Caller.class);
		private MyRmiClient c;

		public Caller(MyRmiClient c) {
			this.c = c;
		}

		public void run() {
			while (true) {
				try {
					c.remoteCall("DateTimeService", "sleep", new Object[] { new Long(1000) });
					log.info("Current Date is:" + c.remoteCall("DateTimeService", "getCurrentDate", new Object[] {}));
				} catch (MyRmiBaseException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Test
	@Ignore("Test required running server, is ignored")
	public void testSingleRemoteCall() {
		try (MyRmiClient singleClient = new MyRmiClient("localhost", 2323);) {
			Object result = singleClient.remoteCall("TextService", "formatText",
					new String[] { "Server executed your command %s.%s(%s)", "TextService", "formatText", "Sergey" });
			System.out.println(String.format("Result: %s", result));
			Thread.sleep(1000);
			singleClient.remoteCall("DateTimeService", "sleep", new Object[] { new Long(1000) });
			System.out.println(String.format("Current Date is:"
					+ singleClient.remoteCall("DateTimeService", "getCurrentDate", new Object[] {})));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	@Ignore("Test required running server, is ignored")
	public void testThreadRemoteCall() {
		try (MyRmiClient threadClient = new MyRmiClient("localhost", 2323);) {
			for (int i = 0; i < THREAD_COUNT; i++) {
				new Thread(new Caller(threadClient)).start();
			}
			Thread.sleep(300000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Interupted threads" + e.getMessage());
		} catch (IOException e1) {
			fail(e1.getMessage());
		}
	}

}
