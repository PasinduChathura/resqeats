package com.ffms.resqeats.enums.master;

public enum WorkflowNodeType {
    WORKFLOW(1, "WORKFLOW"),
    STAGE(2, "STAGE"),
    START(3, "START"),
    END(4, "END");

    private final int id;
    private final String name;

    WorkflowNodeType(int id, String name) {
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
