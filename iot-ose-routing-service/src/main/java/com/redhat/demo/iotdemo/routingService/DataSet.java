package com.redhat.demo.iotdemo.routingService;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@XmlRootElement(name = "dataSet")
@XmlType(propOrder = { "timestamp", "deviceType", "deviceID","count", "payload","required","average" })
@CsvRecord(separator = ",")
public class DataSet implements Serializable {
	@DataField(pos = 1, required = true) 
	private String	deviceType;
	
	@DataField(pos = 2, required = true) 
	private String	deviceID;
	
	@DataField(pos = 3, required = true) 
	private	String	payload;
	
	@DataField(pos = 4, required = true)
	private String	count;
	
	@DataField(pos = 5, required = true) 
	private String	timestamp;
	
	@DataField(pos = 6, required = false)
	private int		required;
	
	@DataField(pos = 7, required = false)
	private float	average;
	
	public DataSet()
	{
		this.timestamp 	= "";
		this.deviceType = "";
		this.deviceID	= "";
		this.count		= "";
		this.payload	= "";
		this.required	= 0;
		this.average	= 0;
	}
	
	public DataSet(String time, String devType, String devID, String count, String pay, int req, float av )
	{
		this.timestamp 	= time;
		this.deviceType = devType;
		this.deviceID	= devID;
		this.count		= count;
		this.payload	= pay;
		this.required	= req;
		this.average	= av;
	}


	/**
	 * @return the timestamp
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the deviceType
	 */
	public String getDeviceType() {
		return deviceType;
	}

	/**
	 * @param deviceType the deviceType to set
	 */
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	/**
	 * @return the deviceID
	 */
	public String getDeviceID() {
		return deviceID;
	}

	/**
	 * @param deviceID the deviceID to set
	 */
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	/**
	 * @return the payload
	 */
	public String getPayload() {
		return payload;
	}

	/**
	 * @param payload the payload to set
	 */
	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	/**
	 * @return the required
	 */
	public int getRequired() {
		return required;
	}

	/**
	 * @param required the required to set
	 */
	public void setRequired(int required) {
		this.required = required;
	}

	/**
	 * @return the average
	 */
	public float getAverage() {
		return average;
	}

	/**
	 * @param average the average to set
	 */
	public void setAverage(float average) {
		this.average = average;
	}
	
	
}