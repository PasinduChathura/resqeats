package com.ffms.resqeats.dto.master.workflow;

import com.ffms.resqeats.enums.master.WorkflowNodeType;
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
