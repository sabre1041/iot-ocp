package com.redhat.demo.iot;

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redhat.demo.iot.mqtt.MqttProducer;

@Configuration
public class Config {
	
	@Value("${mqtt.username}")
	private String mqttUsername;
	
	@Value("${mqtt.password}")
	private String mqttPassword;
		
	@Value("${mqtt.port}")
	private String mqttServicePort;
	
	@Value("${mqtt.service}")
	private String mqttServiceName;
	
	@Value("${device.id}")
	private String deviceId;
	
	@Value("${app.name}")
	private String appName;
	
	@Value("${device.category:#{null}}")
	private String category;
	
	@Value("${device.categories}")
	private String[] categories;
	
	@PostConstruct
	public void init() {
		
		// If category is not defined, choose one randomly
		if(category == null) {
			
			category = categories[ThreadLocalRandom.current().nextInt(categories.length-1)];
			
		}
		
	}
		
	@Bean
	public MqttProducer mqttProducer() {
		
		String brokerURL = String.format("tcp://%s:%s", mqttServiceName, mqttServicePort);
		
		return new MqttProducer(brokerURL, mqttUsername, mqttPassword, appName);
	}
	
	public String getDeviceId() {
		
		return deviceId;
	}
	
	public String getCategory() {
		return category;
	}
	
	public String getAppName() {
		return appName;
	}

}
