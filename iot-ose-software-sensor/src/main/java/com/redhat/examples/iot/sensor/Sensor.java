package com.redhat.examples.iot.sensor;

import com.redhat.examples.iot.model.Measure;

public interface Sensor {
	
	public void initAndReset();
	public int getFrequency();
	public String getType();
	public boolean isEnabled();
	public Measure calculateCurrentMeasure(Measure measure);

}
