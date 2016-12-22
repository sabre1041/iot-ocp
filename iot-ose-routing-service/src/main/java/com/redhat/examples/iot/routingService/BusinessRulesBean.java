package com.redhat.examples.iot.routingService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.camel.Body;
import org.drools.core.command.runtime.rule.GetObjectsCommand;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.internal.command.CommandFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.RuleServicesClient;

public class BusinessRulesBean {
	
	private String kieHost;
	private String kieUser;
	private String kiePassword;
	
	private static final String SERVICE_CONTEXT = "kie-server/services/rest/server";
	
	public Measure processRules(@Body Measure measure) {
		
		KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(
				kieHost, kieUser,
				kiePassword);
		
		Set<Class<?>> jaxBClasses = new HashSet<Class<?>>();
		jaxBClasses.add(Measure.class);
		
		config.addJaxbClasses(jaxBClasses);
		config.setMarshallingFormat(MarshallingFormat.JAXB);
		RuleServicesClient client = KieServicesFactory.newKieServicesClient(config)
				.getServicesClient(RuleServicesClient.class);

        List<Command<?>> cmds = new ArrayList<Command<?>>();
		KieCommands commands = KieServices.Factory.get().getCommands();
		cmds.add(commands.newInsert(measure));
		
	    GetObjectsCommand getObjectsCommand = new GetObjectsCommand();
	    getObjectsCommand.setOutIdentifier("objects");

		
		cmds.add(commands.newFireAllRules());
		cmds.add(getObjectsCommand);
		BatchExecutionCommand myCommands = CommandFactory.newBatchExecution(cmds,
				"DecisionTableKS");
		ServiceResponse<ExecutionResults> response = client.executeCommandsWithResults("iot-ose-businessrules-service", myCommands);
				
		List responseList = (List) response.getResult().getValue("objects");
		
		Measure responseMeasure = (Measure) responseList.get(0);
		
		return responseMeasure;

	}
	
	public void init() {
		
		String kieAppName = (System.getenv("KIE_APP_NAME") == null) ? "kie-app" : System.getenv("KIE_APP_NAME");
		String kiePort = (System.getenv("KIE_APP_SERVICE_PORT") == null) ? "8080" : System.getenv("KIE_APP_SERVICE_PORT");
		
		kieUser = (System.getenv("KIE_APP_USER") == null) ? "iotuser" : System.getenv("KIE_APP_USER");
		kiePassword = (System.getenv("KIE_APP_PASSWORD") == null) ? "iotuser1!" : System.getenv("KIE_APP_PASSWORD");
		
		kieHost = String.format("http://%s:%s/%s", kieAppName, kiePort, SERVICE_CONTEXT);

	}

}
