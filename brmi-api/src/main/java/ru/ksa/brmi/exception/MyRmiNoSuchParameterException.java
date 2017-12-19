package ru.ksa.brmi.exception;

public class MyRmiNoSuchParameterException extends MyRmiBaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyRmiNoSuchParameterException(String message) {
		super(message);
	}

	public MyRmiNoSuchParameterException(Throwable cause) {
		super(cause);
	}

	public MyRmiNoSuchParameterException(String message, Throwable cause) {
		super(message, cause);
	}

}
