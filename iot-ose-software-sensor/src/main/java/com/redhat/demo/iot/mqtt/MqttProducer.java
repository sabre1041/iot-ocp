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

import com.redhat.demo.iot.ScheduledAction;

public class MqttProducer {

	private MqttClient client;
	private MqttConnectOptions options;
	private MemoryPersistence persistence;
	
	private final String brokerURL;
	private final String user;
	private final String password;
	private final String queueName;
	
    private static final Logger log = LoggerFactory.getLogger(ScheduledAction.class);

	
	public MqttProducer(String brokerURL, String user, String password, String queueName) {
	
		this.brokerURL = brokerURL;
		this.user = user;
		this.password = password;
		this.queueName = queueName;
		
	}
	
	public void connect() throws MqttSecurityException, MqttException {
		
		try {
			client = new MqttClient(brokerURL, "mqtt.temp.receiver");
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.out);
		}
		
		options = new MqttConnectOptions ();
		options.setUserName(user);
		options.setPassword(password.toCharArray());
		
		client.connect(options);
		
	}
	
	public void run(String data, String deviceType, String deviceID) throws MqttPersistenceException, MqttException {
		
		MqttMessage message = new MqttMessage();
		
		message.setPayload(data.getBytes());
		
		client.publish("iotdemo/"+deviceType+"/"+deviceID, message);
		
	}
	
	@PreDestroy
	public void close() throws MqttException{
		try { client.disconnect(); } catch(Exception e) {}
		try { client.close(); } catch(Exception e) {}
		
	}
}