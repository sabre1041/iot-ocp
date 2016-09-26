package com.redhat.demo.businessRules;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseTest {
	
	private KieSession session;
	static Logger LOG = LoggerFactory.getLogger(BaseTest.class);
	
    @Before
    public void setup() {
        KieServices kieServices = KieServices.Factory.get();

        KieContainer kContainer = kieServices.getKieClasspathContainer();
        Results verifyResults = kContainer.verify();
        for (Message m : verifyResults.getMessages()) {
            LOG.debug("Kie container message: {}", m);
        }

        KieBase kieBase = kContainer.getKieBase();
        LOG.debug("Created kieBase");

        for ( KiePackage kp : kieBase.getKiePackages() ) {
            for (Rule rule : kp.getRules()) {
                LOG.debug("kp " + kp + " rule " + rule.getName());
            }
        }

        session = kieBase.newKieSession();
        LOG.info(session.getGlobals().toString());
        Logger kieLogger = LoggerFactory.getLogger(this.getClass());
       // session.setGlobal("logger",kieLogger);
    }
    
    protected void executeRules(Object fact) {
        session.insert(fact);
        session.fireAllRules();
    }
    
    @After
    public void teardown() {
        session.dispose();
    }

}
