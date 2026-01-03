package com.ffms.trackable.models.master;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ffms.trackable.common.model.BaseEntity;
import com.ffms.trackable.enums.master.WorkflowType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowSubType extends BaseEntity {
    @OneToMany(mappedBy = "workflowSubType")
    @JsonIgnore
    private List<Workflow> workflows;

    @Column(name = "type", columnDefinition = "VARCHAR(50) DEFAULT 'system'")
    @Enumerated(EnumType.STRING)
    private WorkflowType workflowType;
}
