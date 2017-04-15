package com.taptoscan.taptoscan;

public class Order {
    int id, categoryID, quantity;
    String name, code, symbol, price;
    boolean selected = false;

    Order(int id, int categoryID, int quantity, String name, String code, String symbol, String price, boolean selected) {
        this.id = id;
        this.categoryID = categoryID;
        this.name = name;
        this.code = code;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.selected = selected;
    }

    public void setSelected(boolean val) {
        selected = val;
    }

    public boolean isSelected() {
        return selected;
    }
}
