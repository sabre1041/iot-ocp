package com.redhat.demo.iot.sensor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.redhat.demo.iot.model.Measure;

@Component(SensorType.GPS)
@Scope("prototype")
public class GpsSensor implements Sensor {

	@Value("${sensor.gps.enabled}")
	private boolean enabled;
	
	@Value("${sensor.gps.frequency}")
	private int frequency;
	
	@Value("${sensor.gps.initialLatitude}")
	private double initialLatitude;

	@Value("${sensor.gps.initialLongitude}")
	private double initialLongitude;
	
	@Value("${sensor.gps.iterationLongitude}")
	private double iterationLatitude;
	
	@Value("${sensor.gps.iterationLongitude}")
	private double iterationLongitude;
	
	@Value("${sensor.gps.finalLongitude}")
	private double finalLatitude;
	
	@Value("${sensor.gps.finalLongitude}")
	private double finalLongitude;
	
	private double currentLongitude;
	private double currentLatitude;
	
	private int count = 0;
	
	@PostConstruct
	public void init() {
		currentLongitude = initialLongitude;
		currentLatitude = initialLatitude;
	}

	@Override
	public int getFrequency() {
		return frequency;
	}

	@Override
	public String getType() {
		return SensorType.GPS;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public Measure calculateCurrentMeasure(Measure measure) {
		
		if(count > 0) {
			
			//TODO: Need better logic when determining final values
			
			currentLatitude = currentLatitude + iterationLatitude;
			currentLongitude = currentLongitude + iterationLongitude;
			
			if(currentLatitude <= finalLatitude) {
				currentLatitude = finalLatitude;
			}
			if(currentLongitude <= finalLongitude) {
				currentLongitude = finalLongitude;
			}
			
		}
		
		String payload = formatPayload(currentLatitude, currentLongitude);
		
		measure.setType(getType());
		measure.setPayload(String.valueOf(payload));

		
		++count;
		
		return measure;
		
		
	}
	
	private String formatPayload(double latitude, double longitude) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(latitude);
		sb.append("|");
		sb.append(longitude);
		
		return sb.toString();
	}
	

}
