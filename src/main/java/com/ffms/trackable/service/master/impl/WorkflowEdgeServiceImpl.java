package com.ffms.trackable.service.master.impl;

import com.ffms.trackable.common.service.CommonServiceImpl;
import com.ffms.trackable.models.master.WorkflowEdge;
import com.ffms.trackable.repository.master.WorkflowEdgeRepository;
import com.ffms.trackable.service.master.WorkflowEdgeService;
import org.springframework.stereotype.Service;

@Service
public class WorkflowEdgeServiceImpl extends CommonServiceImpl<WorkflowEdge, Long, WorkflowEdgeRepository> implements WorkflowEdgeService {

    @Override
    public String isValid(WorkflowEdge stage) {
        return null;
    }
}
