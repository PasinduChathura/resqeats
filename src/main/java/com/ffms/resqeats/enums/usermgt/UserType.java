package com.ffms.resqeats.enums.usermgt;

public enum UserType {
    SUPER_ADMIN(1, "SUPER_ADMIN"),
    ADMIN(2, "ADMIN"),
    SHOP_OWNER(3, "SHOP_OWNER"),
    USER(4, "USER");

    private final int id;
    private final String name;

    UserType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static UserType fromName(String name) {
        for (UserType type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return USER;
    }
}
