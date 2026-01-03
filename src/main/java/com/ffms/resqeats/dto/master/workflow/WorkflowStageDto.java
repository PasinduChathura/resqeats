package com.ffms.resqeats.dto.master.workflow;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStageDto {
    private List<WorkflowNodeDto> nodes;
    private List<WorkflowEdgeDto> edges;
}
