package com.redhat.demo.iot.sensor;

import com.redhat.demo.iot.model.Measure;

public interface Sensor {
	
	public int getFrequency();
	public String getType();
	public boolean isEnabled();
	public Measure calculateCurrentMeasure(Measure measure);

}
