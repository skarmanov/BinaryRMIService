package ru.ksa.brmi.exception;

public class MyRmiInvocationException extends MyRmiBaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyRmiInvocationException(String message) {
		super(message);
	}

	public MyRmiInvocationException(Throwable cause) {
		super(cause);
	}

	public MyRmiInvocationException(String message, Throwable cause) {
		super(message, cause);
	}

}
