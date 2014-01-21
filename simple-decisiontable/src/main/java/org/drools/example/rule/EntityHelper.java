package org.drools.example.rule;

import org.drools.example.model.Cheese;

import java.util.Random;

public class EntityHelper {

    private static final Random random = new Random();

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
