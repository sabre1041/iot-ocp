package com.redhat.examples.iot.sensor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;

import com.redhat.examples.iot.model.Measure;

//@Component(SensorType.COUNTDOWN)
@Scope("prototype")
@Deprecated
public class CountdownSensor implements Sensor {
	
	@Value("${sensor.countdown.enabled}") 
	private boolean enabled;
	
	@Value("${sensor.countdown.frequency}")
	public int frequency;
		
	@Value("${sensor.countdown.iteration}")
	public int iteration;
	
	@Value("${sensor.countdown.start}") 
	private int start;
	
	public int currentValue;
	
	public int count = 0;
	
	@PostConstruct
	public void init() {
		currentValue = start;
	}
	

	@Override
	public int getFrequency() {
		return frequency;
	}
	
	@Override
	public String getType() {
		return SensorType.COUNTDOWN;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public Measure calculateCurrentMeasure(Measure measure) {
		
		
		if(count > 0) {
			currentValue = currentValue - iteration;
		}
		
		measure.setType(getType());
		measure.setPayload(String.valueOf(currentValue));
		
		
		// TODO: Figure out how to handle current time
		
		++count;
		return measure;
	}


	@Override
	public void initAndReset() {
		// TODO Auto-generated method stub
		
	}

	

}
