package org.drools.example.osgi;

import org.drools.example.model.Person;
import org.drools.example.rule.PersonHelper;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.cdi.KSession;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.ops4j.pax.cdi.api.ContainerInitialized;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class CanDrinkRuleOsgiCdiWeld {

    @Inject
    @KSession("sampleKSession")
    private KieSession ksession;

    public void onStartup(@Observes ContainerInitialized event) {

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

    @PreDestroy
    public void onClose() {
        if (this.ksession != null) {
            this.ksession.dispose();
            System.out.println("KieSession disposed.");
        }
    }


}
