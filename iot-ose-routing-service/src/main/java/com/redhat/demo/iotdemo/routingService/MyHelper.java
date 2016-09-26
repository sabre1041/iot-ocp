
package com.redhat.demo.iotdemo.routingService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;

public class MyHelper {
	
	private int TOPIC_PART_SIZE = 3;
	private static final String TOPIC_SEPARTOR = "/";
	private static final String COMMA = ",";
	
    private static final ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue()
        {
            return new SimpleDateFormat("dd.MM.yyy HH:mm:ss SSS");
        }
    };
	
	@Handler
	public String enhanceMessage( String body,  Exchange exchange  ) {
		String res = null;
		
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
		sb.append(COMMA);
		sb.append(dateFormat.get().format(new Date()));
		
		return sb.toString();
	}
   
    @Handler
    public String appendTimestamp( String body,  Exchange exchange ) {
       
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyy HH:mm:ss SSS");
        Date timeNow = new Date();

        body = body +","+format.format(timeNow);
             
        return body;
       
    }
}

