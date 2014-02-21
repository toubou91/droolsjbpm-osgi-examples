package org.drools.example.osgi;

import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Created by chmoulli on 18/02/14.
 */
public class ScanOsgiActivator implements BundleActivator {

    private KieScanner kScanner;
    private KieSession ksession;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        KieServices kieServices = KieServices.Factory.get();

        ReleaseId releaseId = kieServices.newReleaseId( "org.test", "kie-project-simple", "LATEST" );

        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        KieContainer kContainer = kieServices.newKieContainer( releaseId );

        kScanner = kieServices.newKieScanner( kContainer );

        for (int i = 0; i < 100; i++) {

            kScanner.scanNow();

            ksession = kContainer.newKieSession();
            ksession.insert("test");
            ksession.fireAllRules();

            Thread.sleep(5000);
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