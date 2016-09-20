package com.redhat.demo.iot;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.redhat.demo.iot.mqtt.DummyDataGenerator;
import com.redhat.demo.iot.mqtt.MqttProducer;

@Component
public class ScheduledAction {
		
	@Autowired
	private MqttProducer mqttProducer;
	
    private static final String	DEFAULT_DEVICETYPE   	= "temperature";
    private static final String DEFAULT_DEVICEID     	= "1";
    private static final String DEFAULT_INITIALVALUE 	= "70";
    private static final String DEFAULT_COUNT 		 	= "1";
    private static final String DEFAULT_UNIT		 	= "C";
    private static final String DEFAULT_WAIT		 	= "1";
    private static final String DEFAULT_HIGHWATER_MARK 	= "800";
    private static final String DEFAULT_LOWWATER_MARK 	= "200";

	
    private static final Logger log = LoggerFactory.getLogger(ScheduledAction.class);

    @Scheduled(fixedDelayString="${scheduledtask.interval}")
    public void reportCurrentTime() throws MqttPersistenceException, MqttException {

    	DummyDataGenerator dummy = new DummyDataGenerator();
       
        String devType 	 	  = System.getProperty("deviceType", DEFAULT_DEVICETYPE);
        String devID		  = System.getProperty("deviceID", DEFAULT_DEVICEID);
        int initialValue 	  = Integer.parseInt(System.getProperty("initialValue", DEFAULT_INITIALVALUE));
//        int count 		 	  = Integer.parseInt(System.getProperty("count", DEFAULT_COUNT));
//        int waitTime 		  = Integer.parseInt(System.getProperty("waitTime", DEFAULT_WAIT));
        String unit			  = System.getProperty("payloadUnit", DEFAULT_UNIT);        
        int highWater 	 	 = Integer.parseInt(System.getProperty("highWater", DEFAULT_HIGHWATER_MARK));
        int lowWater 	 	 = Integer.parseInt(System.getProperty("lowWater", DEFAULT_LOWWATER_MARK));
        
        dummy.createInitialDataSet(devType, devID, initialValue, unit, highWater, lowWater); 
		
        mqttProducer.connect();
        System.out.println("Sending '"+dummy.getDataSetCSV()+"'");
        mqttProducer.run( dummy.getDataSetCSV(), dummy.getDataSet().getDeviceType(), dummy.getDataSet().getDeviceID() );
        dummy.updateDataSet(); 
        mqttProducer.close();

    	
    }
    
}
