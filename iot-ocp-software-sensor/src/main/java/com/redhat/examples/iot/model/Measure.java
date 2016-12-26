package com.redhat.examples.iot.model;

import java.io.Serializable;

public class Measure implements Serializable {

	private static final long serialVersionUID = -6134767246202226552L;
	
	private String  type;
	private final String  deviceId;
	private final String category;
	private final String timestamp;
	private	String	payload;
	
	public Measure(String deviceId, String timestamp, String category) {
		this.deviceId = deviceId;
		this.timestamp = timestamp;
		this.category = category;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getPayload() {
		return payload;
	}
	public void setPayload(String payload) {
		this.payload = payload;
	}
	
	public String getCategory() {
		return category;
	}
	
	public String getCSVData() {
		StringBuilder sb = new StringBuilder();
		sb.append(category);
		sb.append(",");
		sb.append(payload);
		sb.append(",");
		sb.append(timestamp);
		return sb.toString();
	}

}
