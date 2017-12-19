package ru.ksa.brmi.server.test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ru.ksa.brmi.exception.MyRmiBaseException;
import ru.ksa.brmi.exception.MyRmiClassNotFoundException;
import ru.ksa.brmi.exception.MyRmiNoSuchMethodException;
import ru.ksa.brmi.exception.MyRmiNoSuchParameterException;
import ru.ksa.brmi.server.impl.MyRmiServiceLocator;
import ru.ksa.brmi.server.services.MyTestService;

public class MyRmiServiceLocatorTest {
	private static final String TEST_SERVICE_KEY = "TextService";
	MyTestService testService = new MyTestService();

	@Before
	public void setUp() throws Exception {
		MyRmiServiceLocator.INSTANCE.registerService(TEST_SERVICE_KEY, testService.getClass().getName());
	}

	@After
	public void tearDown() throws Exception {
		MyRmiServiceLocator.INSTANCE.removeService(TEST_SERVICE_KEY);
	}

	@Test
	public void testRegisterService_Ok() {
		try {
			MyRmiServiceLocator.INSTANCE.registerService(TEST_SERVICE_KEY, testService.getClass().getName());
		} catch (MyRmiBaseException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testRegisterService_BadClassName() {
		try {
			MyRmiServiceLocator.INSTANCE.registerService(TEST_SERVICE_KEY, testService.getClass().getName() + "bad");
		} catch (MyRmiBaseException e) {
			assertThat("Inalid exception type", e, instanceOf(MyRmiClassNotFoundException.class));
		}
	}

	@Test
	public void testInvokeServiceByObject_Ok() {
		try {
			MyRmiServiceLocator.invokeService(testService, "formatText", new String[] {
					"Server executed your command %s.%s(%s)", TEST_SERVICE_KEY, "formatText", "test parameter" });
		} catch (MyRmiBaseException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testInvokeServiceByObject_NoSuchMethod() {
		try {
			MyRmiServiceLocator.invokeService(testService, "badMethod", new Object[] {
					"Server executed your command %s.%s(%s)", TEST_SERVICE_KEY, "formatText", "test parameter" });
			fail("Invoke Bad Method");
		} catch (MyRmiBaseException e) {
			assertThat("Inalid exception type", e, instanceOf(MyRmiNoSuchMethodException.class));
		}
	}

	@Test
	public void testInvokeServiceByObject_InvalidArgumentType() {
		try {
			MyRmiServiceLocator.invokeService(testService, "formatText", new Object[] {
					"Server executed your command %s.%s(%s)", TEST_SERVICE_KEY, "formatText", Integer.valueOf(11) });
			fail("Invoke Method with Illegal Argument");
		} catch (MyRmiBaseException e) {
			assertThat("Inalid exception type", e, instanceOf(MyRmiNoSuchParameterException.class));
		}
	}

	/**
	 * 
	 */
	@Test
	public void testInvokeServiceByObject_InvalidArgumentCount() {
		try {
			MyRmiServiceLocator.invokeService(testService, "formatText",
					new Object[] { "Server executed your command %s.%s(%s)", TEST_SERVICE_KEY, "formatText",
							"badArgument", Integer.valueOf(11) });
			fail("Invoke Method with Illegal Argument");
		} catch (MyRmiBaseException e) {
			assertThat("Inalid exception type", e, instanceOf(MyRmiNoSuchParameterException.class));
		}
	}

	@Test
	public void testInvokeServiceByName() {
		try {
			MyRmiServiceLocator.invokeService(TEST_SERVICE_KEY, "formatText", new Object[] {
					"Server executed your command %s.%s(%s)", TEST_SERVICE_KEY, "formatText", "test parameter" });
		} catch (MyRmiBaseException e) {
			fail(e.getMessage());
		}
	}
}
