package com.redhat.demo.iot.mqtt;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "dataSet")
@XmlType(propOrder = { "timestamp", "deviceType", "deviceID", "count", "payload", "unit", "required"})
public class TestDataSet {
	private String	timestamp;
	private String  deviceType;
	private String  deviceID;	
	private int		count;
	private	int		payload;
	private int		required;
	private String	unit;
	
	public TestDataSet()
	{
		this.timestamp 	= "";
		this.deviceType = "";
		this.deviceID	= "";
		this.count		= 0;
		this.payload	= 0;
		this.required	= 0;
		this.unit		= "";
	}
	
	public TestDataSet(String time, String devType, String devID, int count, String unit, int pay )
	{
		this.timestamp 	= time;
		this.deviceType = devType;
		this.deviceID	= devID;
		this.count		= count;
		this.payload	= pay;
		this.unit		= unit;
		this.required	= 0;
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
	public int getPayload() {
		return payload;
	}

	/**
	 * @param payload the payload to set
	 */
	public void setPayload(int payload) {
		this.payload = payload;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
	
}
