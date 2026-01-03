package com.ffms.trackable.dto.master.workflow;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowEdgeDto {
    private String id;
    private String source;
    private String target;
    private String targetHandle;
    private String sourceHandle;
}
