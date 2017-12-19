package ru.ksa.brmi.api;

import java.io.Serializable;

public abstract class MyRmiResult<T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final T value;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public MyRmiResult() {
		this.value = null;
	}

	public MyRmiResult(T value) {
		this.value = value;
	}

	public T get() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

}
