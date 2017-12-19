package ru.ksa.brmi.server.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ MyRmiServiceLocatorTest.class, MyRmiServiceProcessorTest.class })
public class AllServerTests {

}
