package ru.ksa.brmi.exception;

public class MyRmiNoSuchMethodException extends MyRmiBaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyRmiNoSuchMethodException(String message) {
		super(message);
	}

	public MyRmiNoSuchMethodException(Throwable cause) {
		super(cause);
	}

	public MyRmiNoSuchMethodException(String message, Throwable cause) {
		super(message, cause);
	}

}
