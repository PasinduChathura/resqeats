package com.ffms.resqeats.enums.usermgt;

public enum PrivilegeType {
    SUPER_ADMIN(1, "SUPER_ADMIN"),
    USER(2, "USER"),
    WORKFLOW(3, "WORKFLOW"),
    ROLE(4, "ROLE"),
    SHOP(5, "SHOP"),
    FOOD(6, "FOOD"),
    ORDER(7, "ORDER"),
    CART(8, "CART"),
    PAYMENT(9, "PAYMENT"),
    NOTIFICATION(10, "NOTIFICATION"),
    CUSTOMER(11, "CUSTOMER");

    private final int id;
    private final String name;

    PrivilegeType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
