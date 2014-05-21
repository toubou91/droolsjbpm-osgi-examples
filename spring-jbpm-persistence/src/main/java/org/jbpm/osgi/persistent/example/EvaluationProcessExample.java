package org.jbpm.osgi.persistent.example;

import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class EvaluationProcessExample {

    private static final Logger logger = LoggerFactory.getLogger(EvaluationProcessExample.class);

    private KieSession ksession;
    private Environment env;

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

    public Environment getEnv() {
        return env;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }


}
