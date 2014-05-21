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
    private KieSession kSession;
    private kieThread kieThread;

    public void start(BundleContext bundleContext) {
        kieThread = new kieThread();
        kieThread.start();
    }

    public void stop(BundleContext bundleContext) throws Exception {
        kieThread.stopThread();
    }

    public class kieThread extends Thread {

        private volatile boolean active = true;

        public void run() {
            while (active) {
                KieServices kieServices = KieServices.Factory.get();

                ReleaseId releaseId = kieServices.newReleaseId("org.test",
                        "kie-project-simple", "1.0");

                KieContainer kContainer = kieServices.newKieContainer(releaseId);

                kScanner = kieServices.newKieScanner(kContainer);

                System.out.println("This is a Kie-Ci example. The drl rule is packaged " +
                        "as a kmodule in a jar and deployed in your maven repo");

                // Scan to discover new resources of an existing artifact
                kScanner.scanNow();

                for (int i = 0; i < 100; i++) {
                    // Create a stateless session
                    kSession = kContainer.newKieSession();
                    kSession.insert("Hello");
                    kSession.fireAllRules();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        System.out.println("Thread interrupted. Firing of rules will stop.");
                    }
                }

            }
        }

        public void stopThread() {
            active = false;
            if (kScanner != null) {
                kScanner.stop();
                System.out.println("KieScanner stopped.");
            }
            if (kSession != null) {
                kSession.dispose();
                System.out.println("KieSession disposed.");
            }
        }
    }
}
