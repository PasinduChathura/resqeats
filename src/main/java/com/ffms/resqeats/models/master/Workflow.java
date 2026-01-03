package com.ffms.resqeats.models.master;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ffms.resqeats.common.model.BaseEntity;
import com.ffms.resqeats.enums.master.WorkflowType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workflow extends BaseEntity {
    @Column(name = "type", columnDefinition = "VARCHAR(50) DEFAULT 'system'")
    @Enumerated(EnumType.STRING)
    private WorkflowType workflowType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sub_type", referencedColumnName = "id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private WorkflowSubType workflowSubType;

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkflowNode> nodes;

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkflowEdge> edges;
}
