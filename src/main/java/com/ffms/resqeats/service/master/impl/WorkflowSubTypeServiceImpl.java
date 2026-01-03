package com.ffms.resqeats.service.master.impl;

import com.ffms.resqeats.common.service.CommonServiceImpl;
import com.ffms.resqeats.models.master.WorkflowSubType;
import com.ffms.resqeats.repository.master.WorkflowSubTypeRepository;
import com.ffms.resqeats.service.master.WorkflowSubTypeService;
import org.springframework.stereotype.Service;

@Service
public class WorkflowSubTypeServiceImpl extends CommonServiceImpl<WorkflowSubType, Long, WorkflowSubTypeRepository> implements WorkflowSubTypeService {

    @Override
    public String isValid(WorkflowSubType subType) {
        return null;
    }
}
