package com.redhat.demo;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

public class MqttTester {
	
	// Default Values for message producer
    private static final String	DEFAULT_DEVICETYPE   	= "temperature";
    private static final String DEFAULT_DEVICEID     	= "1";
    private static final String DEFAULT_INITIALVALUE 	= "70";
    private static final String DEFAULT_COUNT 		 	= "1";
    private static final String DEFAULT_UNIT		 	= "C";
    private static final String DEFAULT_WAIT		 	= "1";
    private static final String DEFAULT_RECEIVER		= "localhost";
    private static final String DEFAULT_BROKER_UID		= "iotuser";
    private static final String DEFAULT_BROKER_PASSWD 	= "iotuser";
    private static final String DEFAULT_HIGHWATER_MARK 	= "800";
    private static final String DEFAULT_LOWWATER_MARK 	= "200";
    
    
    public static void main(String args[]) throws MqttPersistenceException, MqttException, InterruptedException {
    	DummyDataGenerator dummy = new DummyDataGenerator();
    	MqttProducer		   producer;
       
        String devType 	 	  = System.getProperty("deviceType", DEFAULT_DEVICETYPE);
        String devID		  = System.getProperty("deviceID", DEFAULT_DEVICEID);
        int initialValue 	  = Integer.parseInt(System.getProperty("initialValue", DEFAULT_INITIALVALUE));
        int count 		 	  = Integer.parseInt(System.getProperty("count", DEFAULT_COUNT));
        int waitTime 		  = Integer.parseInt(System.getProperty("waitTime", DEFAULT_WAIT));
        String unit			  = System.getProperty("payloadUnit", DEFAULT_UNIT);
        String brokerURLMQTT  = "tcp://" + System.getProperty("receiverURL",DEFAULT_RECEIVER) +  ":1883";
        String brokerUID 	  = System.getProperty("brokerUID", DEFAULT_BROKER_UID);
        String brokerPassword = System.getProperty("brokerPassword", DEFAULT_BROKER_PASSWD);
        
        int highWater 	 	 = Integer.parseInt(System.getProperty("highWater", DEFAULT_HIGHWATER_MARK));
        int lowWater 	 	 = Integer.parseInt(System.getProperty("lowWater", DEFAULT_LOWWATER_MARK));
        
        dummy.createInitialDataSet(devType, devID, initialValue, unit, highWater, lowWater); 
       	
    	producer = new MqttProducer(brokerURLMQTT, brokerUID, brokerPassword, "mqtt.receiver");
        
        int counter = 0;
        while ( counter < count ) {
			
    		System.out.println("Sending '"+dummy.getDataSetCSV()+"'");
    		producer.run( dummy.getDataSetCSV(), dummy.getDataSet().getDeviceType(), dummy.getDataSet().getDeviceID() );
        	
			dummy.updateDataSet();
			
			counter++;
			
			Thread.sleep ( waitTime * 1000 );
        }
		    
        producer.close();

    }

}
