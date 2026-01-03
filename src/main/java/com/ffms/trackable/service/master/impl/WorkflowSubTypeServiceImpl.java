package com.ffms.trackable.service.master.impl;

import com.ffms.trackable.common.service.CommonServiceImpl;
import com.ffms.trackable.models.master.WorkflowSubType;
import com.ffms.trackable.repository.master.WorkflowSubTypeRepository;
import com.ffms.trackable.service.master.WorkflowSubTypeService;
import org.springframework.stereotype.Service;

@Service
public class WorkflowSubTypeServiceImpl extends CommonServiceImpl<WorkflowSubType, Long, WorkflowSubTypeRepository> implements WorkflowSubTypeService {

    @Override
    public String isValid(WorkflowSubType subType) {
        return null;
    }
}
