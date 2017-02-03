package com.redhat.examples.iot.sensor;

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.redhat.examples.iot.model.Measure;

@Component(SensorType.TEMPERATURE)
@Scope("prototype")
public class TemperatureSensor implements Sensor {
	
	@Value("${sensor.temperature.enabled}") 
	private boolean enabled;
	
	@Value("${sensor.temperature.frequency}")
	public int frequency;
		
	@Value("${sensor.temperature.startMin}") 
	private int startMin;
	
	@Value("${sensor.temperature.startMax}") 
	private int startMax;

	
	@Value("${sensor.temperature.minIteration}") 
	private int minIteration;
	
	@Value("${sensor.temperature.maxIteration}") 
	private int maxIteration;

		
	@Value("${sensor.temperature.minRange}") 
	private int minRange;

	@Value("${sensor.temperature.maxRange}") 
	private int maxRange;
	
	public double currentValue;
	
	public int count = 0;
	
	@PostConstruct
	@Override
	public void initAndReset() {
		currentValue = ThreadLocalRandom.current().nextInt(startMin, (startMax+1));
	}
	

	@Override
	public int getFrequency() {
		return frequency;
	}
	
	@Override
	public String getType() {
		return SensorType.TEMPERATURE;
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
