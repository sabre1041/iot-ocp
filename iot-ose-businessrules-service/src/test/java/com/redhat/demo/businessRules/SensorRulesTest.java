package com.redhat.demo.businessRules;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SensorRulesTest extends BaseTest {
	    
    @Test
    public void organTemperatureTooHigh() {
    	
    	Measure measure = new Measure();
    	measure.setDataType("temperature");
    	measure.setCategory("Organ");
    	measure.setPayload("105");
    	    	
    	executeRules(measure);
    	
    	assertEquals(1,measure.getErrorCode());
    }


}
