package ru.ksa.brmi.exception;

public class MyRmiBaseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyRmiBaseException(String message) {
		super(message);
	}

	public MyRmiBaseException(Throwable cause) {
		super(cause);
	}

	public MyRmiBaseException(String message, Throwable cause) {
		super(message, cause);
	}

}
