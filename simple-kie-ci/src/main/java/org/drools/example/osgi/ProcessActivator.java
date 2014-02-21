package org.drools.example.osgi;

import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ProcessActivator implements BundleActivator {

    private KieScanner kScanner;
    private KieSession ksession;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        KieServices kieServices = KieServices.Factory.get();

        ReleaseId releaseId = kieServices.newReleaseId( "org.test", "kie-project-simple", "1.0" );

        KieContainer kContainer = kieServices.newKieContainer( releaseId );

        kScanner = kieServices.newKieScanner( kContainer );

        System.out.println("This is a Kie-Ci example. The drl rule is packaged as a kmodule in a jar and deployed in your maven repo");

        for (int i = 0; i < 100; i++) {

            kScanner.scanNow();

            ksession = kContainer.newKieSession();
            ksession.insert("Hello");
            ksession.fireAllRules();

            Thread.sleep(10000);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if (this.kScanner != null) {
            this.kScanner.stop();
            System.out.println("KieSscanner stopped.");
        }
        if (this.ksession != null) {
            this.ksession.dispose();
            System.out.println("KieSession disposed.");
        }
    }
}
