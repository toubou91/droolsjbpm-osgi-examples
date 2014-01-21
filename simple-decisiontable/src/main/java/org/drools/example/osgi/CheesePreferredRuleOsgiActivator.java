package org.drools.example.osgi;

import org.drools.example.model.Cheese;
import org.drools.example.rule.EntityHelper;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieSession;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class CheesePreferredRuleOsgiActivator implements BundleActivator {

    private KieSession ksession;

    public void start(final BundleContext bc) throws Exception {

        KieServices ks = KieServices.Factory.get();
        KieBaseConfiguration kbaseConfig = ks.newKieBaseConfiguration(null, this.getClass().getClassLoader());
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        KieBase kbase = ks.newKieClasspathContainer().newKieBase(kbaseConfig);

        this.ksession = kbase.newKieSession();
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

    public void stop(final BundleContext bc) throws Exception {
        if (this.ksession != null) {
            this.ksession.dispose();
            System.out.println("KieSession disposed.");
        }
    }

}
