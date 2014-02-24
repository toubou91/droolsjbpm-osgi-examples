package org.drools.example.osgi;

import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.NoSuchElementException;

public class ProcessActivator implements BundleActivator {

    private KieScanner kScanner;
    private KieSession kSession;

    @Override
    public void start(BundleContext bundleContext) {
        try {
            KieServices kieServices = KieServices.Factory.get();

            ReleaseId releaseId = kieServices.newReleaseId("org.test", "kie-project-simple", "1.0");

            KieContainer kContainer = kieServices.newKieContainer(releaseId);

            kScanner = kieServices.newKieScanner(kContainer);

            System.out.println("This is a Kie-Ci example. The drl rule is packaged as a kmodule in a jar and deployed in your maven repo");

            for (int i = 0; i < 100; i++) {

                kScanner.scanNow();

                kSession = kContainer.newKieSession();
                kSession.insert("Hello");
                kSession.fireAllRules();

                Thread.sleep(10000);
            }
        } catch (InterruptedException e) {
            System.out.println("Ctrl-c command executed. Process interrupted");
        } catch (NoSuchElementException ne) {
            ne.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if (this.kScanner != null) {
            this.kScanner.stop();
            System.out.println("KieScanner stopped.");
        }
        if (this.kSession != null) {
            this.kSession.dispose();
            System.out.println("KieSession disposed.");
        }
    }
}
