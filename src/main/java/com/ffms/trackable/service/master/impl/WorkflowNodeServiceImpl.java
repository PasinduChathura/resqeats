package com.ffms.trackable.service.master.impl;

import com.ffms.trackable.common.service.CommonServiceImpl;
import com.ffms.trackable.models.master.WorkflowNode;
import com.ffms.trackable.repository.master.WorkflowNodeRepository;
import com.ffms.trackable.service.master.WorkflowNodeService;
import org.springframework.stereotype.Service;

@Service
public class WorkflowNodeServiceImpl extends CommonServiceImpl<WorkflowNode, Long, WorkflowNodeRepository> implements WorkflowNodeService {

    @Override
    public String isValid(WorkflowNode stage) {
        return null;
    }
}
