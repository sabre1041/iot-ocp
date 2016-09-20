package com.redhat.demo.iot.mqtt;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
 
public class DummyDataGenerator {

	private TestDataSet tempSet;
	private int		lowWaterThreshold;
	private int		highWaterThreshold;
	
	public void createInitialDataSet(String  devType, String devID, int pay, String unit, int highWater, int lowWater ) {
		tempSet = new TestDataSet();
		
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyy HH:mm:ss SSS");
		Date timeNow = new Date();
		
		tempSet.setTimestamp( format.format(timeNow) );
		tempSet.setDeviceType(devType);
		tempSet.setDeviceID(devID);
		tempSet.setPayload(pay);
		tempSet.setUnit(unit);
		tempSet.setCount(0);
		setLowWaterThreshold(lowWater);
		setHighWaterThreshold(highWater);
	}
	
	public void updateDataSet() {
		Random random = new Random();

		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyy HH:mm:ss SSS");
		Date timeNow = new Date();
		
		tempSet.setTimestamp( format.format(timeNow) );
		
		int randValue = random.nextInt(1000);
		
		tempSet.setCount( tempSet.getCount() + 1 );
		
		if ( randValue <= getLowWaterThreshold() )
			tempSet.setPayload(tempSet.getPayload()-1);
		else if ( randValue >= getHighWaterThreshold() )
			tempSet.setPayload(tempSet.getPayload()+1);
	}
	
	public TestDataSet getDataSet(){
		return tempSet;
	}
	
	public String getDataSetXML() {
		return jaxbObjectToXML();
	}
	
	public String getDataSetCSV() {
		StringBuilder sb = new StringBuilder();
		
		sb.append( tempSet.getPayload() ).append(",");
		sb.append( tempSet.getCount() );
		
		return sb.toString();
	}
	
	private String jaxbObjectToXML( ) {
		 
		StringWriter writer = new StringWriter();
        
        try {
            JAXBContext context = JAXBContext.newInstance(TestDataSet.class);
            
            Marshaller m = context.createMarshaller();
            
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 
            m.marshal(this.getDataSet(), writer);
           
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        
        return writer.toString();
	}
	
	/**
	 * @return the lowWaterThreshold
	 */
	public int getLowWaterThreshold() {
		return lowWaterThreshold;
	}

	/**
	 * @param lowWaterThreshold the lowWaterThreshold to set
	 */
	public void setLowWaterThreshold(int lowWaterThreshold) {
		this.lowWaterThreshold = lowWaterThreshold;
	}

	/**
	 * @return the highWaterThreshold
	 */
	public int getHighWaterThreshold() {
		return highWaterThreshold;
	}

	/**
	 * @param highWaterThreshold the highWaterThreshold to set
	 */
	public void setHighWaterThreshold(int highWaterThreshold) {
		this.highWaterThreshold = highWaterThreshold;
	}

	
}
