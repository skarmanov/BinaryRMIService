package ru.ksa.brmi.api;

import java.io.Serializable;
import java.util.Arrays;

public class MyRmiRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Integer requestNo;
	private final String serviceName;
	private final String method;
	private final Object[] params;

	/**
	 * Remote server request
	 * 
	 * @param requestNo
	 *            request sequence number
	 * @param serviceName
	 *            service name
	 * @param method
	 *            method name
	 * @param params
	 *            parameters for method
	 */
	public MyRmiRequest(Integer requestNo, String serviceName, String method, Object[] params) {
		this.requestNo = requestNo;
		this.serviceName = serviceName;
		this.method = method;
		this.params = params;
	}

	public Integer getRequestNo() {
		return requestNo;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getMethod() {
		return method;
	}

	public Object[] getParams() {
		return params;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Request [requestNo=");
		builder.append(requestNo);
		builder.append(", serviceName=");
		builder.append(serviceName);
		builder.append(", method=");
		builder.append(method);
		builder.append(", params=");
		builder.append(Arrays.toString(params));
		builder.append("]");
		return builder.toString();
	}

}
