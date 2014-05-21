package org.drools.example.osgi;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.drools.example.model.Person;
import org.drools.example.rule.PersonHelper;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.osgi.framework.BundleContext;

@Component(name = "org.osgi.framework.BundleActivator")
public class CanDrinkRuleOsgiDeclarativeService {

    private KieSession ksession;

    @Activate
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

    @Deactivate
    public void stop(final BundleContext bc) throws Exception {
        if (this.ksession != null) {
            this.ksession.dispose();
            System.out.println("KieSession disposed.");
        }
    }

}
