package com.redhat.demo.iot.mqtt;

import javax.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttProducer {

	private MqttClient client;
	private MqttConnectOptions options;
	private MemoryPersistence persistence;
	
	private final String brokerURL;
	private final String user;
	private final String password;
	private final String appName;

	private static final int QOS = 2;
	
    private static final Logger log = LoggerFactory.getLogger(MqttProducer.class);

	
	public MqttProducer(String brokerURL, String user, String password, String appName) {
	
		this.brokerURL = brokerURL;
		this.user = user;
		this.password = password;
		this.appName = appName;
		
	}
	
	public synchronized void connect() {
		
		if(client != null && client.isConnected()) {
			return;
		}
		
		try {
			client = new MqttClient(brokerURL, appName);
			options = new MqttConnectOptions ();
			options.setUserName(user);
			options.setPassword(password.toCharArray());
			client.connect(options);
		}catch(MqttException e) {
			log.error(e.getMessage(), e);
		}
		
	}
	
	public void run(String topic, String data) throws MqttPersistenceException, MqttException {
		
		MqttMessage message = new MqttMessage();
		message.setQos(QOS);
		
		message.setPayload(data.getBytes());
		client.publish(topic, message);
				
	}
	
	@PreDestroy
	public void close() throws MqttException{
		try { client.disconnect(); } catch(Exception e) {}
		try { client.close(); } catch(Exception e) {}
		
	}
}