package com.redhat.demo.iot;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.demo.iot.model.Measure;
import com.redhat.demo.iot.mqtt.MqttProducer;
import com.redhat.demo.iot.sensor.Sensor;

public class SensorRunner implements Runnable {
	
    private static final Logger log = LoggerFactory.getLogger(SchedulerManager.class);
    
    private static final ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue()
        {
            return new SimpleDateFormat("yyyyMMdd HHmm");
        }
    };

    private final Sensor sensor;
    
    private final String appName;
    
    private final String deviceId;
    
    private final String category;
    
    private final MqttProducer mqttProducer;
    
    private String topicName;
    
    private static final String SLASH = "/";
    private static final String SW_CODE = "sw";
    
    public SensorRunner(Sensor sensor, Config config, MqttProducer mqttProducer) {
    	this.sensor = sensor;
    	this.appName = config.getAppName();
    	this.deviceId = config.getDeviceId();
    	this.category = config.getCategory();
    	this.mqttProducer = mqttProducer;
    	
    	generateTopicName();
    }
    
	@Override
	public void run() {

		Measure measure = new Measure(deviceId, dateFormat.get().format(new Date()), category);
		
		sensor.calculateCurrentMeasure(measure);
		
		//if(log.isDebugEnabled()) {
			log.info("Current Measure ["+measure.getType()+"]: " + measure.getPayload());
		//}

		try {
			mqttProducer.run(topicName, measure.getCSVData());
		} catch (MqttException e) {
			log.error(e.getMessage(), e);
		}
						
	}
	
	private void generateTopicName() {
		StringBuilder sb = new StringBuilder();
		sb.append(appName);
		sb.append(SLASH);
		sb.append(SW_CODE);
		sb.append(SLASH);
		sb.append(sensor.getType());
		sb.append(SLASH);
		sb.append(deviceId);
		
		topicName = sb.toString();
	}

}
