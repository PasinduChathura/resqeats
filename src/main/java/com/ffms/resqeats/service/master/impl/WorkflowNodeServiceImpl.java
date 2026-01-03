package com.ffms.resqeats.service.master.impl;

import com.ffms.resqeats.common.service.CommonServiceImpl;
import com.ffms.resqeats.models.master.WorkflowNode;
import com.ffms.resqeats.repository.master.WorkflowNodeRepository;
import com.ffms.resqeats.service.master.WorkflowNodeService;
import org.springframework.stereotype.Service;

@Service
public class WorkflowNodeServiceImpl extends CommonServiceImpl<WorkflowNode, Long, WorkflowNodeRepository> implements WorkflowNodeService {

    @Override
    public String isValid(WorkflowNode stage) {
        return null;
    }
}
