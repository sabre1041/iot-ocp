package com.redhat.examples.iot.businessRules;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "measure")
@XmlType(propOrder = { "timestamp", "dataType", "sensorType", "deviceId", "category", "payload", "errorCode", "errorMessage" })
public class Measure {
	
	private String sensorType;
	private String dataType;
	private String  deviceId;
	private String category;
	private String timestamp;
	private	String	payload;
	private int errorCode;
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
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getPayload() {
		return payload;
	}
	public void setPayload(String payload) {
		this.payload = payload;
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

	
	public String asString( ) throws JAXBException {
		StringWriter sw = new StringWriter();
		
		JAXBContext context = JAXBContext.newInstance("com.redhat.examples.iot.businessRules");
	    Marshaller marshaller = context.createMarshaller();
	    marshaller.marshal(this, sw );
	    
	    return sw.toString();
	    
	}
	
}
