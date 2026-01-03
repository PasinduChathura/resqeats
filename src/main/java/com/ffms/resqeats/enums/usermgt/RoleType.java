package com.ffms.resqeats.enums.usermgt;

public enum RoleType {
    SUPER_ADMIN(1, "SUPER_ADMIN"),
    ADMIN(2, "ADMIN"),
    SHOP_OWNER(3, "SHOP_OWNER"),
    USER(4, "USER");

    private final int id;
    private final String name;

    RoleType(int id, String name) {
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
