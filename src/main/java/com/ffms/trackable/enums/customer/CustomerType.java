package com.ffms.trackable.enums.customer;

public enum CustomerType {
    PARENT(1, "PARENT"), CHILD(2, "CHILD");

    private final int id;
    private final String name;

    CustomerType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static CustomerType fromName(String name) {
        if (name == null) {
            return CustomerType.PARENT;
        }

        for (CustomerType type : CustomerType.values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("invalid customer type: " + name);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
