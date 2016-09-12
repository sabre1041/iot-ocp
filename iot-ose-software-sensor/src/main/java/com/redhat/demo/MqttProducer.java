package com.redhat.demo;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttProducer {

	MqttClient client;
	MqttConnectOptions options;
	MemoryPersistence persistence;
	
	public MqttProducer(String brokerURL, String user, String password, String queueName) {
	
		// MemoryPersistence persistence = new MemoryPersistence();
		
		try {
			client = new MqttClient(brokerURL, "mqtt.temp.receiver");
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.out);
		}
		
		options = new MqttConnectOptions ();
		options.setUserName(user);
		options.setPassword(password.toCharArray());
		
		try {
			client.connect(options);
		} catch (MqttSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.out);
		}
		
	}
	
	public void run(String data, String deviceType, String deviceID) throws MqttPersistenceException, MqttException {
		
		MqttMessage message = new MqttMessage();
		
		message.setPayload(data.getBytes());
		
		client.publish("iotdemo/"+deviceType+"/"+deviceID, message);
		
	}
	
	public void close() throws MqttException{
		
		client.disconnect();
		client.close();
		
	}
}