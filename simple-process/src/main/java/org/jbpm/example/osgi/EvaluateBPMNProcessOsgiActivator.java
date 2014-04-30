package org.jbpm.example.osgi;

import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.runtime.manager.impl.RuntimeEnvironmentBuilder;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class EvaluateBPMNProcessOsgiActivator implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(EvaluateBPMNProcessOsgiActivator.class);
    private static final String SIMPLE_PROCESS = "org/jbpm/example/process/Evaluation.bpmn2";

    private RuntimeManager runtimeManager;

    private KieSession ksession;
    private RuntimeEngine runtimeEngine;

    public void start(BundleContext context) throws Exception {

        LOG.info("Loading EvaluationProcess.bpmn2");

        runtimeManager = getRuntimeManager(SIMPLE_PROCESS);
        runtimeEngine = runtimeManager.getRuntimeEngine(EmptyContext.get());
        ksession = runtimeEngine.getKieSession();

        LOG.info("Register tasks");
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("RegisterRequest", new SystemOutWorkItemHandler());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "UserId-12345");

        LOG.info("Start process Evaluation (bpmn2)");
        ProcessInstance processInstance = ksession.startProcess("Evaluation", params);
        LOG.info("Stated completed");

    }

    public void stop(BundleContext context) throws Exception {
        if (ksession != null) {
            ksession.dispose();

            runtimeManager.disposeRuntimeEngine(runtimeEngine);
            runtimeManager.close();

            System.out.println("KieSession disposed.");
        }

    }

    private RuntimeManager getRuntimeManager(String process) {
        RuntimeEnvironment environment = RuntimeEnvironmentBuilder.getEmpty()
                .addAsset(ResourceFactory.newClassPathResource(process,getClass().getClassLoader()), ResourceType.BPMN2)
                .get();
        return RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);
    }
}
