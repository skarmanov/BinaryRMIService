package ru.ksa.brmi.server.services;

import java.util.Date;

public class MyDateTimeService {

	public MyDateTimeService() {
		super();
	}

	public void sleep(Long millis) throws InterruptedException {
		Thread.sleep(millis.longValue());
	}

	public Date getCurrentDate() {
		return new Date();
	}

}
