package com.ffms.resqeats.service.master;

import com.ffms.resqeats.common.dto.StandardResponse;
import com.ffms.resqeats.common.service.CommonService;
import com.ffms.resqeats.dto.master.workflow.WorkflowDto;
import com.ffms.resqeats.models.master.Workflow;

public interface WorkflowService extends CommonService<Workflow, Long> {
    StandardResponse<?> createWorkflow(WorkflowDto workflowDto) throws Exception;

    StandardResponse<?> getWorkflows() throws Exception;

    StandardResponse<?> getWorkflowById(Long id) throws Exception;

}