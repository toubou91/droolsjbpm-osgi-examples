package org.drools.example.osgi;

import org.drools.example.model.Cheese;
import org.drools.example.rule.EntityHelper;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieSession;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class FetchExternalResourceOsgiActivator implements BundleActivator {

    private static final ReleaseId APP_REL_ID = KieServices.Factory.get()
            .newReleaseId("org.drools.example", "fetch-external-resource", "1.0");
    private static final String EXTERNAL_XLS_RESOURCE = "file:///Users/chmoulli/MyProjects/droolsjbpm-osgi-examples/documentation/decision-table/cheeseDecisionTable.xls";

    private KieSession ksession;

    @Override
    public void start(BundleContext context) throws Exception {

        KieServices ks = KieServices.Factory.get();
        ks.newKieClasspathContainer(getClass().getClassLoader());
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        KieBase kbase = createKieBase();

        ksession = kbase.newKieSession();
        System.out.println("KieSession created.");

        for (int i = 0; i < 10; i++) {
            // Create a Cheese
            Cheese aCheese = EntityHelper.createCheese();
            ksession.insert(aCheese);

            // Fire the rules
            ksession.fireAllRules();

            // Check Cheese Price
            EntityHelper.cheesePrice(aCheese);
        }

        System.out.println("Cheese added and rules fired.");

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (ksession != null) {
            ksession.dispose();
            System.out.println("KieSession disposed.");
        }
    }

    private KieBase createKieBase() {

        final String PACKAGE_NAME = "org.drools.example.cheese";

        KieServices ks = KieServices.Factory.get();
        Resource rs = ks.getResources().newUrlResource(EXTERNAL_XLS_RESOURCE);

        KieFileSystem kfs = ks.newKieFileSystem()
                .generateAndWritePomXML(APP_REL_ID)
                .write(rs)
                .writeKModuleXML(createKieProjectWithPackages(ks, PACKAGE_NAME).toXML());

        ks.newKieBuilder( kfs ).buildAll();

        KieBuilder kbuilder = ks.newKieBuilder(kfs);
        kbuilder.buildAll();

        return ks.newKieContainer(APP_REL_ID).getKieBase();
    }

    private KieModuleModel createKieProjectWithPackages(KieServices ks, String pkg) {
        KieModuleModel kmodule = ks.newKieModuleModel();
        kmodule.newKieBaseModel("KBase")
               .addPackage(pkg)
               .setDefault(true)
               .newKieSessionModel("defaultSession")
                  .setDefault(true);

        return kmodule;
    }
}
