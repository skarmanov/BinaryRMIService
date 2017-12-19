package ru.ksa.brmi.exception;

public class MyRmiClassNotFoundException extends MyRmiBaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyRmiClassNotFoundException(String message) {
		super(message);
	}

	public MyRmiClassNotFoundException(Throwable cause) {
		super(cause);
	}

	public MyRmiClassNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
