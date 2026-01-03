package com.ffms.resqeats.service.master.impl;

import com.ffms.resqeats.common.service.CommonServiceImpl;
import com.ffms.resqeats.models.master.WorkflowEdge;
import com.ffms.resqeats.repository.master.WorkflowEdgeRepository;
import com.ffms.resqeats.service.master.WorkflowEdgeService;
import org.springframework.stereotype.Service;

@Service
public class WorkflowEdgeServiceImpl extends CommonServiceImpl<WorkflowEdge, Long, WorkflowEdgeRepository> implements WorkflowEdgeService {

    @Override
    public String isValid(WorkflowEdge stage) {
        return null;
    }
}
