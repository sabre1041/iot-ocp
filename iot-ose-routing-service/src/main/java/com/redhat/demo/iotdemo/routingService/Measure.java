package com.redhat.demo.iotdemo.routingService;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@XmlRootElement(name = "measure")
@XmlType(propOrder = { "timestamp", "dataType", "sensorType", "deviceId", "category", "payload", "errorCode", "errorMessage" })
@CsvRecord(separator = ",")
public class Measure implements Serializable {

	@DataField(pos = 1, required = true) 
	private String category;
	
	@DataField(pos = 2, required = true)
	private String payload;
	
	@DataField(pos = 3, required = true) 
	private String timestamp;
	
	@DataField(pos = 4, required = true) 
	private String sensorType;

	@DataField(pos = 5, required = true)
	private String dataType;
		
	@DataField(pos = 6, required = true) 
	private String deviceId;
	
	@DataField(pos = 7, required = false) 
	private int errorCode;
	
	@DataField(pos = 8, required = false) 
	private String errorMessage;


	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public String getSensorType() {
		return sensorType;
	}

	public void setSensorType(String sensorType) {
		this.sensorType = sensorType;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	
	
}
