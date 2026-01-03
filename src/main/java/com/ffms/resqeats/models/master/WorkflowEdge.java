package com.ffms.resqeats.models.master;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ffms.resqeats.common.model.BaseEntity;
import com.ffms.resqeats.enums.master.WorkflowNodeType;
import com.ffms.resqeats.models.usermgt.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowEdge extends BaseEntity {
    @Column(unique = true)
    private String edgeId;

    @ManyToOne
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @JoinColumn(name = "workflow_id")
    private Workflow workflow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_node_id")
    private WorkflowNode source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_node_id")
    private WorkflowNode target;

    private String targetHandle;

    private String sourceHandle;
}
