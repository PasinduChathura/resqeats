package com.ffms.trackable.dto.master.workflow;

import com.ffms.trackable.enums.master.WorkflowNodeType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowNodeDto {
    private String id;
    private String name;
    private WorkflowNodeType type;
    private String style;
}
