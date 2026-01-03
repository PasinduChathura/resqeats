package com.ffms.resqeats.models.master;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ffms.resqeats.common.model.BaseEntity;
import com.ffms.resqeats.enums.master.WorkflowNodeType;
import com.ffms.resqeats.models.usermgt.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowNode extends BaseEntity {
    @Column(unique = true)
    private String nodeId;
    @ManyToOne
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @JoinColumn(name = "workflow_id")
    private Workflow workflow;

    @ManyToMany(cascade = { CascadeType.ALL })
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @JoinTable(
            name = "workflow_stage_role",
            joinColumns = { @JoinColumn(name = "stage_id") },
            inverseJoinColumns = { @JoinColumn(name = "role_id") }
    )
    private Set<Role> roles;

    private WorkflowNodeType nodeType;

    private String style;

    @OneToMany(mappedBy = "source", cascade = CascadeType.ALL)
    private List<WorkflowEdge> outgoingEdges;

    @OneToMany(mappedBy = "target", cascade = CascadeType.ALL)
    private List<WorkflowEdge> incomingEdges;
}
