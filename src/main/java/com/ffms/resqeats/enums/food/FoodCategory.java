package com.ffms.resqeats.enums.food;

public enum FoodCategory {
    BREAD("BREAD"),
    PASTRY("PASTRY"),
    DAIRY("DAIRY"),
    FRUITS("FRUITS"),
    VEGETABLES("VEGETABLES"),
    PREPARED_MEAL("PREPARED_MEAL"),
    SANDWICH("SANDWICH"),
    SALAD("SALAD"),
    DESSERT("DESSERT"),
    BEVERAGE("BEVERAGE"),
    SNACKS("SNACKS"),
    OTHER("OTHER");

    private final String value;

    FoodCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
