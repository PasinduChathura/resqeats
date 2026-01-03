package com.ffms.trackable.enums.usermgt;

public enum RoleType {
    SUPER_ADMIN(1, "SUPER_ADMIN"),
    USER(2, "USER");

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
