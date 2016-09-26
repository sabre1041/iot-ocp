package com.redhat.demo.businessRules;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SensorRulesTest extends BaseTest {
	    
    @Test
    public void organTemperatureTooLow() {
    	
    	Measure measure = new Measure();
    	measure.setDataType("temperature");
    	measure.setCategory("Organ");
    	measure.setPayload("0");
    	    	
    	executeRules(measure);
    	
    	assertEquals(1,measure.getErrorCode());
    }


}
