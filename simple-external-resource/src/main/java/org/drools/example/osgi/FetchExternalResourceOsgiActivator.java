package org.drools.example.osgi;

import org.drools.example.rule.EntityHelper;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
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
            .newReleaseId("org.drools.example", "fetch-external-resource", "1.0.0-SNAPSHOT");

    private KieSession ksession;

    @Override
    public void start(BundleContext context) throws Exception {

        KieServices ks = KieServices.Factory.get();
        KieBaseConfiguration kbaseConfig = ks.newKieBaseConfiguration(null, this.getClass().getClassLoader());
        KieBase kbase = this.createKieBase(kbaseConfig);

        this.ksession = kbase.newKieSession();
        System.out.println("KieSession created.");

        // Add a Person & fires rule
        this.ksession.insert(EntityHelper.createPerson());
        int count = ksession.fireAllRules();

        System.out.println(">> Rule fired - count result : " + count);

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.ksession != null) {
            this.ksession.dispose();
            System.out.println("KieSession disposed.");
        }
    }

    private KieBase createKieBase(KieBaseConfiguration kbaseConfig) {

/*      Resource rs = ResourceFactory.newUrlResource("file:///Users/chmoulli/MyProjects/droolsjbpm-osgi-examples/documentation/decision-table/cheeseDecisionTable.xls");
        KieFileSystem kfs = ks.newKieFileSystem().write( rs );
        KieBuilder kb = ks.newKieBuilder( kfs ).buildAll();*/

        /*final String PACKAGE_NAME = "org.drools.example.cheese"; */
        final String PACKAGE_NAME = "org.drools.example.external";

        String drl = "package org.drools.example.external\n" +
                "import org.drools.example.model.Person\n" +
                "rule R1 when\n" +
                "   p : Person( age >= 18 )\n" +
                "then\n" +
                "   p.setCanDrink( true )\n" +
                "end\n";

        KieServices ks = KieServices.Factory.get();
        Resource drlResource = ks.getResources().newByteArrayResource(drl.getBytes());
        Resource rs = ks.getResources().newUrlResource("file:///Users/chmoulli/MyProjects/droolsjbpm-osgi-examples/documentation/decision-table/cheeseDecisionTable.xls");
        drlResource.setSourcePath("src/main/resources/org/drools/example/fetch/canDrink.drl");

        KieFileSystem kfs = ks.newKieFileSystem()
                .generateAndWritePomXML(APP_REL_ID)
                .write(drlResource)
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
