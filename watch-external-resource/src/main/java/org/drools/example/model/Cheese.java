package org.drools.example.model;
public class Cheese {

    private String type;
    private int price;

    public Cheese() {

    }
    public Cheese(final String type,
                  final int price) {
        super();
        this.type = type;
        this.price = price;
    }

    public int getPrice() {
        return this.price;
    }

    public String getType() {
        return this.type;
    }

    public void setPrice(final int price) {
        this.price = price;
    }

    public void setType(String type) {
        this.type = type;
    }

}
