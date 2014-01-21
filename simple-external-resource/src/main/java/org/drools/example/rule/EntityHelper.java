package org.drools.example.rule;

import org.drools.example.model.Cheese;
import org.drools.example.model.Person;

import java.util.Random;

public class EntityHelper {

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

    public static Cheese createCheese() {
        Cheese cheese = new Cheese();
        if (random.nextBoolean()) {
            cheese.setType("Stilton");
            cheese.setPrice(10);
        } else {
            cheese.setType("Cheddar");
            cheese.setPrice(50);
        }
        return cheese;
    }

    public static void cheesePrice(Cheese aCheese) {
        System.out.println("Cheese " + aCheese.getType() + " costs " + aCheese.getPrice() + " EUR.");
    }
}
