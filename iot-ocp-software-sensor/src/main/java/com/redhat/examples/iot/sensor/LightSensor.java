package com.redhat.examples.iot.sensor;

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.redhat.examples.iot.model.Measure;

@Component(SensorType.LIGHT)
@Scope("prototype")
public class LightSensor implements Sensor {
	
	@Value("${sensor.light.enabled}") 
	private boolean enabled;
	
	@Value("${sensor.light.frequency}")
	public int frequency;
		
	@Value("${sensor.light.start}") 
	private int start;
	
	@Value("${sensor.light.maxIteration}") 
	private int maxIteration;
	
	@Value("${sensor.light.minIteration}") 
	private int minIteration;
	
	@Value("${sensor.light.minRange}") 
	private int minRange;

	@Value("${sensor.light.maxRange}") 
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
		return SensorType.LIGHT;
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

		++count;
		return measure;
	}

	

}
