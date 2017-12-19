package ru.ksa.brmi.server.services;

public class MyTestService {

	public MyTestService() {
		super();
	}

	public String formatText(String format, String valueOne, String valueTwo, String valueThree) {
		return String.format(format, valueOne, valueTwo, valueThree);
	}
}
