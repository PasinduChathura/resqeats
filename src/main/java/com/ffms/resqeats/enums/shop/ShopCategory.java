package com.ffms.resqeats.enums.shop;

public enum ShopCategory {
    BAKERY("BAKERY"),
    GROCERY("GROCERY"),
    RESTAURANT("RESTAURANT"),
    FRUIT_VEGETABLE("FRUIT_VEGETABLE"),
    CAFE("CAFE"),
    SUPERMARKET("SUPERMARKET"),
    OTHER("OTHER");

    private final String value;

    ShopCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
