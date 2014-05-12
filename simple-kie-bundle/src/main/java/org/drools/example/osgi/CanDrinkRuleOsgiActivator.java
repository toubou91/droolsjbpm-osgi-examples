package org.drools.example.osgi;

import org.drools.example.model.Person;
import org.drools.example.rule.PersonHelper;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

public class CanDrinkRuleOsgiActivator implements BundleActivator {

    private KieSession ksession;

    public void start(final BundleContext bc) throws Exception {

        KieServices ks = KieServices.Factory.get();
        KieContainer kcont = ks.newKieClasspathContainer(getClass().getClassLoader());
        KieBase kbase = kcont.getKieBase("sampleKBase");

        this.ksession = kbase.newKieSession();
        System.out.println("KieSession created.");

        for (int i = 0; i < 20; i++) {
            // Create a Person
            Person aPerson = PersonHelper.createPerson();
            ksession.insert(aPerson);

            // Fire the rules
            ksession.fireAllRules();

            // Check if it can drink
            PersonHelper.canDrink(aPerson);
        }
    }

    public void stop(final BundleContext bc) throws Exception {
        if (this.ksession != null) {
            this.ksession.dispose();
            System.out.println("KieSession disposed.");
        }
    }

}
