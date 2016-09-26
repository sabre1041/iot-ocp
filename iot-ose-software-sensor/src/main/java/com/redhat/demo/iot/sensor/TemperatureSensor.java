package com.redhat.demo.iot.sensor;

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.redhat.demo.iot.model.Measure;

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

	
	@Value("${sensor.temperature.iteration}") 
	private float iteration;
		
	@Value("${sensor.temperature.minRange}") 
	private int minRange;

	@Value("${sensor.temperature.maxRange}") 
	private int maxRange;
	
	public float currentValue;
	
	public int count = 0;
	
	@PostConstruct
	public void init() {
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

			
			
			currentValue = ThreadLocalRandom.current().nextInt() % 2 == 0 ? (currentValue + iteration) : (currentValue - iteration);
						
			if(currentValue < minRange) {
				currentValue = minRange;
			}
			
			if(currentValue > maxRange) {
				currentValue = maxRange;
			}
			
		}
		
		measure.setType(getType());
		measure.setPayload(String.valueOf(currentValue));
		
		
		// TODO: Figure out how to handle current time
		
		++count;
		return measure;
	}

	

}
