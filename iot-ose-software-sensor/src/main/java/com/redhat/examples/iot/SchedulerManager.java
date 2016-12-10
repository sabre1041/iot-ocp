package com.redhat.examples.iot;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.redhat.examples.iot.mqtt.MqttProducer;
import com.redhat.examples.iot.sensor.Sensor;

@Component
public class SchedulerManager implements ApplicationContextAware {
	
    private static final Logger log = LoggerFactory.getLogger(SchedulerManager.class);
    
    private ApplicationContext applicationContext;
    
    @Value("${iot.types}")
    private String[] iotTypes;
    
    @Autowired
    private Config config;
    
    @Autowired
    private MqttProducer mqttProducer;
    
    private ScheduledExecutorService executorService;
    
    @PostConstruct
    public void startup() {
    	log.info("Starting up...");
    	
    	// Environment Information
    	log.info("---------------------------");
    	log.info("Application Name: {}",config.getAppName());
    	log.info("Category: {}",config.getCategory());
    	log.info("Device ID: {}",config.getDeviceId());
    	log.info("---------------------------");


    	executorService = Executors.newScheduledThreadPool(iotTypes.length);
    	
    	for(String iotType : iotTypes) {

    		try {
    			
        		Sensor sensor = applicationContext.getBean(iotType, Sensor.class);
        		        		
        		if(sensor.isEnabled()) {
        			
        			mqttProducer.connect();
        			
            		log.info("Starting Sensor: " + sensor.getType());
        			executorService.scheduleAtFixedRate(new SensorRunner(sensor, config, mqttProducer), 0, sensor.getFrequency(), TimeUnit.SECONDS);
        		}
        		else {
        			log.info("Sensor type " + iotType + " disabled");
        		}
        		
    		}
    		catch(NoSuchBeanDefinitionException nsbde) {
    			log.warn("Sensor type " + iotType + " not available");
    		}
    	
    	}
    	
    }
    
    @PreDestroy
    public void shutdown() {
    	log.info("Shutting Down");
    	
    	if(executorService != null) {
    		executorService.shutdownNow();
    	}
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


}
