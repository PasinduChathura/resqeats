package com.ffms.trackable.service.master;

import com.ffms.trackable.common.dto.StandardResponse;
import com.ffms.trackable.common.service.CommonService;
import com.ffms.trackable.dto.master.workflow.WorkflowDto;
import com.ffms.trackable.models.master.Workflow;

public interface WorkflowService extends CommonService<Workflow, Long> {
    StandardResponse<?> createWorkflow(WorkflowDto workflowDto) throws Exception;

    StandardResponse<?> getWorkflows() throws Exception;

    StandardResponse<?> getWorkflowById(Long id) throws Exception;

}