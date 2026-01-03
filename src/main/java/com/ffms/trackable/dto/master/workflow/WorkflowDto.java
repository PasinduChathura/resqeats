package com.ffms.trackable.dto.master.workflow;

import com.ffms.trackable.enums.master.WorkflowType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowDto {
    private Long id;
    private String name;
    private WorkflowType type;
    private WorkflowSubTypeDto subType;
    private WorkflowStageDto stages;
}