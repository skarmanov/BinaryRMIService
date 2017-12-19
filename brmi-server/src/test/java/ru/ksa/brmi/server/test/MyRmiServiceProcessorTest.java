package ru.ksa.brmi.server.test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.ksa.brmi.api.MyRmiObjectResult;
import ru.ksa.brmi.api.MyRmiRequest;
import ru.ksa.brmi.api.MyRmiResponse;
import ru.ksa.brmi.api.MyRmiVoidResult;
import ru.ksa.brmi.server.impl.MyRmiServerConnection;
import ru.ksa.brmi.server.impl.MyRmiServiceLocator;
import ru.ksa.brmi.server.services.MyDateTimeService;
import ru.ksa.brmi.server.services.MyTestService;

public class MyRmiServiceProcessorTest {
	private static final String TEST_SERVICE_KEY = "TextService";
	private static final String DATETIME_SERVICE_KEY = "DateTimeService";
	private int requestNo = 1;

	MyTestService defaultTestService = new MyTestService();
	MyDateTimeService datetimeService = new MyDateTimeService();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		MyRmiServiceLocator.INSTANCE.registerService(TEST_SERVICE_KEY, defaultTestService.getClass().getName());
		MyRmiServiceLocator.INSTANCE.registerService(DATETIME_SERVICE_KEY, datetimeService.getClass().getName());

	}

	@After
	public void tearDown() throws Exception {
		MyRmiServiceLocator.INSTANCE.removeService(TEST_SERVICE_KEY);
		MyRmiServiceLocator.INSTANCE.removeService(DATETIME_SERVICE_KEY);
	}

	@Test
	public void testCall() {
		/**
		 * Default valid test request
		 */
		MyRmiRequest request = new MyRmiRequest(Integer.valueOf(requestNo++), TEST_SERVICE_KEY, "formatText",
				new String[] { "Server executed your command %s.%s(%s)", TEST_SERVICE_KEY, "formatText",
						"test argument" });
		/**
		 * Default Service Processor with default valid request
		 */
		MyRmiServerConnection.RequestProcessor testProcessor = new MyRmiServerConnection.RequestProcessor(request);
		try {
			MyRmiResponse response = testProcessor.call();
			assertEquals(request.getRequestNo(), response.getRequestNo());
			assertThat(response.getResult(), instanceOf(MyRmiObjectResult.class));
		} catch (Exception e) {
			e.printStackTrace();
			fail(String.format("Exception Invoke valid test Method: %s", e.getMessage()));
		}

	}

	@Test
	public void testCallVoidMethod() {
		MyRmiRequest request = new MyRmiRequest(Integer.valueOf(requestNo++), DATETIME_SERVICE_KEY, "sleep",
				new Object[] { new Long(1000) });
		/**
		 * Default Service Processor with default valid request
		 */
		MyRmiServerConnection.RequestProcessor servicePocessor = new MyRmiServerConnection.RequestProcessor(request);
		try {
			MyRmiResponse response = servicePocessor.call();
			assertEquals(request.getRequestNo(), response.getRequestNo());
			assertThat(response.getResult(), instanceOf(MyRmiVoidResult.class));
		} catch (Exception e) {
			e.printStackTrace();
			fail(String.format("Exception Invoke valid test Method: %s", e.getMessage()));
		}
	}

	@Test
	public void testCallCurrentDateMethod() {
		MyRmiRequest request = new MyRmiRequest(Integer.valueOf(requestNo++), DATETIME_SERVICE_KEY, "getCurrentDate",
				new Object[] {});
		/**
		 * Default Service Processor with default valid request
		 */
		MyRmiServerConnection.RequestProcessor servicePocessor = new MyRmiServerConnection.RequestProcessor(request);
		try {
			MyRmiResponse response = servicePocessor.call();
			assertEquals(request.getRequestNo(), response.getRequestNo());
			assertThat(response.getResult(), instanceOf(MyRmiObjectResult.class));
		} catch (Exception e) {
			e.printStackTrace();
			fail(String.format("Exception Invoke valid test Method: %s", e.getMessage()));
		}
	}
}
