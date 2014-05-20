package org.jbpm.osgi.example;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.runtime.manager.impl.RuntimeEnvironmentBuilder;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.io.ResourceFactory;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvaluationProcessExample {

    private static final Logger logger = LoggerFactory.getLogger(EvaluationProcessExample.class);

    private KieSession ksession;

    public static void main(String[] args) throws Exception {
        EvaluationProcessExample pr = new EvaluationProcessExample();
        pr.init();
        pr.destroy();
    }

    public void init() throws Exception {
        logger.info("Loading EvaluationProcess.bpmn2");

        logger.info("Register tasks");
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("RegisterRequest", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "UserId-12345");

        logger.info("Start process EvaluationProcess.bpmn2");
        ProcessInstance processInstance = ksession.startProcess("Evaluation", params);
        logger.info("Stated completed");
    }

    public void destroy() {
        ksession.destroy();
    }

    public KieSession getKsession() {
        return ksession;
    }

    public void setKsession(KieSession ksession) {
        this.ksession = ksession;
    }

}
