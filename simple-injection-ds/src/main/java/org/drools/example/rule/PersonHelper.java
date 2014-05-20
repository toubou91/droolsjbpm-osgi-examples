package org.drools.example.rule;

import org.drools.example.model.Person;

import java.util.Random;

public class PersonHelper {

    private static final Random random = new Random();

    public static Person createPerson() {
        Person person = new Person();
        if (random.nextBoolean()) {
            person.setName("Old Person");
            person.setAge(21);
        } else {
            person.setName("Young Person");
            person.setAge(18);
        }
        return person;
    }

    public static void canDrink(Person aPerson) {
        if (aPerson.isCanDrink()) {
            System.out.println("Person " + aPerson.getName() + " aged of " + aPerson.getAge() + " , can go to the Bar");
        } else {
            System.out.println("Person " + aPerson.getName() + " aged of " + aPerson.getAge() + ", can't go to the Bar");
        }
    }
}
