package ru.ksa.brmi.api;

public class MyRmiObjectResult<T> extends MyRmiResult<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyRmiObjectResult() {
		super();
	}

	public MyRmiObjectResult(T value) {
		super(value);
	}
}
