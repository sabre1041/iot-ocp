
package com.redhat.examples.iot.routingService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.Headers;

public class MyHelper {
	
	private int TOPIC_PART_SIZE = 3;
	private static final String TOPIC_SEPARTOR = "/";
	private static final String COMMA = ",";
	
	@Handler
	public String enhanceMessage( String body,  Exchange exchange  ) {
		
		String[] topicParts = exchange.getIn().getHeader("CamelMQTTSubscribeTopic", String.class).split(TOPIC_SEPARTOR);

		if(topicParts.length != 4) {
			throw new IllegalArgumentException("Invalid number of topic components. Expected " + TOPIC_PART_SIZE + ". Was " + topicParts.length);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(body);
		sb.append(COMMA);
		sb.append(topicParts[1]);
		sb.append(COMMA);
		sb.append(topicParts[2]);
		sb.append(COMMA);
		sb.append(topicParts[3]);
		
		return sb.toString();
	}
   

    public void prepareJdbcHeaders(@Body Measure measure, @Headers Map<String, Object> headers) {

    	headers.put("sensor_type", measure.getSensorType());
    	headers.put("data_type", measure.getDataType());
    	headers.put("device_id", measure.getDeviceId());
    	headers.put("category", measure.getCategory());
    	headers.put("payload", measure.getPayload());
    	headers.put("error_code", measure.getErrorCode());
    	headers.put("error_message", measure.getErrorMessage());
    	headers.put("time_stamp", measure.getTimestamp());

    }
    
}

