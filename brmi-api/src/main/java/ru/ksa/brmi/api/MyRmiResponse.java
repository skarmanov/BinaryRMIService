package ru.ksa.brmi.api;

import java.io.Serializable;

import ru.ksa.brmi.exception.MyRmiBaseException;

public class MyRmiResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Integer requestNo;
	private final MyRmiResult<?> result;
	private final MyRmiBaseException exception;
	private final StatusType status;

	/**
	 * Service execution result
	 * 
	 * @param status
	 *            Status response OK or ERROR
	 * @param requestNo
	 *            request number
	 * @param result
	 *            Object result service execution;
	 */
	protected MyRmiResponse(Integer requestNo, StatusType status, MyRmiResult<?> result) {
		this.requestNo = requestNo;
		this.status = status;
		this.result = result;
		this.exception = null;
	}

	protected MyRmiResponse(Integer requestNo, StatusType status, MyRmiBaseException exception) {
		this.requestNo = requestNo;
		this.status = status;
		this.result = null;
		this.exception = exception;
	}

	public static MyRmiResponseTypeBuilder OK(Integer requestNo) {
		return MyRmiResponseTypeBuilder.OK(requestNo);
	}

	public static MyRmiResponseTypeBuilder ERROR(Integer requestNo, MyRmiBaseException exception) {
		return MyRmiResponseTypeBuilder.ERROR(requestNo, exception);
	}

	public Integer getRequestNo() {
		return requestNo;
	}

	public MyRmiResult<?> getResult() {
		return result;
	}

	public StatusType getStatus() {
		return status;
	}

	public MyRmiBaseException getException() {
		return exception;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MyRmiResponse [requestNo=");
		builder.append(requestNo);
		builder.append(", result=");
		builder.append(result);
		builder.append(", exception=");
		builder.append(exception);
		builder.append(", status=");
		builder.append(status);
		builder.append("]");
		return builder.toString();
	}

	public static enum StatusType {
		OK, ERROR
	}

	public static class MyRmiResponseTypeBuilder {
		private Integer requestNo;
		private StatusType status;
		private MyRmiResult<?> result;
		private MyRmiBaseException exception;

		private MyRmiResponseTypeBuilder(Integer requestNo, StatusType status) {
			this.requestNo = requestNo;
			this.status = status;
		}

		protected static MyRmiResponseTypeBuilder OK(Integer requestNo) {
			return new MyRmiResponseTypeBuilder(requestNo, StatusType.OK);
		}

		protected static MyRmiResponseTypeBuilder ERROR(Integer requestNo, MyRmiBaseException exception) {
			return new MyRmiResponseTypeBuilder(requestNo, StatusType.ERROR).exception(exception);
		}

		public MyRmiResponseTypeBuilder exception(MyRmiBaseException exception) {
			this.exception = exception;
			return this;
		}

		public MyRmiResponseTypeBuilder result(MyRmiResult<?> result) {
			this.result = result;
			return this;
		}

		public MyRmiResponse build() {
			if (status == StatusType.OK)
				return new MyRmiResponse(requestNo, status, result);
			else
				return new MyRmiResponse(requestNo, status, exception);
		}

	}

}
