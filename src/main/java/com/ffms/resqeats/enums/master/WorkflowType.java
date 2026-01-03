package com.ffms.resqeats.enums.master;

public enum WorkflowType {
    SERVICE_REQUEST(1, "SERVICE-REQUEST"),
    ESTIMATION(2, "ESTIMATION"),
    SERVICE_AGREEMENTS(3, "SERVICE-AGREEMENTS"),
    CLAIMS(4, "CLAIMS");

    private final int id;
    private final String name;

    WorkflowType(int id, String name) {
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
