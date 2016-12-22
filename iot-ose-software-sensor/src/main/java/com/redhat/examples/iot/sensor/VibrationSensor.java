package com.redhat.examples.iot.sensor;

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.redhat.examples.iot.model.Measure;

@Component(SensorType.VIBRATION)
@Scope("prototype")
public class VibrationSensor implements Sensor {
	
	@Value("${sensor.vibration.enabled}") 
	private boolean enabled;
	
	@Value("${sensor.vibration.frequency}")
	public int frequency;
		
	@Value("${sensor.vibration.start}") 
	private int start;
	
	@Value("${sensor.vibration.maxIteration}") 
	private int maxIteration;
	
	@Value("${sensor.vibration.minIteration}") 
	private int minIteration;
	
	@Value("${sensor.vibration.minRange}") 
	private int minRange;

	@Value("${sensor.vibration.maxRange}") 
	private int maxRange;
	
	public double currentValue;
	
	public int count = 0;
	
	@PostConstruct
	@Override
	public void initAndReset() {
		currentValue = start;
	}
	

	@Override
	public int getFrequency() {
		return frequency;
	}
	
	@Override
	public String getType() {
		return SensorType.VIBRATION;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public Measure calculateCurrentMeasure(Measure measure) {
		
		
		if(count > 0) {
			
			// Calculate random value from range
			double randValue = ThreadLocalRandom.current().nextDouble(minIteration, (maxIteration+1));
			currentValue = currentValue + randValue;
			
			if(currentValue < minRange || currentValue > maxRange) {
				initAndReset();
			}
			
		}
		
		measure.setType(getType());
		measure.setPayload(String.valueOf(currentValue));
		
		
		// TODO: Figure out how to handle current time
		
		++count;
		return measure;
	}

	

}
