package com.ffms.resqeats.dto.master.workflow;

import com.ffms.resqeats.enums.master.WorkflowType;
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