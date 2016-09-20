package com.redhat.demo.iot.mqtt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
	
	@Value("${mqtt.username}")
	private String mqttUsername;
	
	@Value("${mqtt.password}")
	private String mqttPassword;
	
	@Value("${mqtt.username}")
	private String mqttQueueName;
	
	@Value("${mqtt.port}")
	private String mqttServicePort;
	
	@Value("${mqtt.service}")
	private String mqttServiceName;
	
	@Bean
	public MqttProducer mqttProducer() {
		
		String brokerURL = String.format("tcp://%s:%s", mqttServiceName, mqttServicePort);
		
		return new MqttProducer(brokerURL, mqttUsername, mqttPassword, mqttQueueName);
	}

}
