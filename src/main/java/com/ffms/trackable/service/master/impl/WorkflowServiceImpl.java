package com.ffms.trackable.service.master.impl;

import com.ffms.trackable.common.dto.StandardResponse;
import com.ffms.trackable.common.service.CommonServiceImpl;
import com.ffms.trackable.dto.master.workflow.WorkflowDto;
import com.ffms.trackable.dto.master.workflow.WorkflowEdgeDto;
import com.ffms.trackable.dto.master.workflow.WorkflowNodeDto;
import com.ffms.trackable.dto.master.workflow.WorkflowStageDto;
import com.ffms.trackable.models.master.Workflow;
import com.ffms.trackable.models.master.WorkflowEdge;
import com.ffms.trackable.models.master.WorkflowNode;
import com.ffms.trackable.models.master.WorkflowSubType;
import com.ffms.trackable.repository.master.WorkflowRepository;
import com.ffms.trackable.service.master.WorkflowService;
import com.ffms.trackable.service.master.WorkflowNodeService;
import com.ffms.trackable.service.master.WorkflowSubTypeService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkflowServiceImpl extends CommonServiceImpl<Workflow, Long, WorkflowRepository> implements WorkflowService {

    @Autowired
    WorkflowNodeService workflowNodeService;

    @Autowired
    WorkflowSubTypeService workflowSubTypeService;

    @Override
    public String isValid(Workflow workflow) {
        return null;
    }

    @Override
    public StandardResponse<?> createWorkflow(WorkflowDto workflowDto) throws Exception {

        Workflow workflow = new Workflow();
        workflow.setName(workflowDto.getName());
        workflow.setWorkflowType(workflowDto.getType());

        WorkflowSubType subType = workflowSubTypeService.findById(workflowDto.getSubType().getId());
        workflow.setWorkflowSubType(subType);

        List<WorkflowNodeDto> nodeDtos = workflowDto.getStages().getNodes();
        List<WorkflowEdgeDto> edgeDtos = workflowDto.getStages().getEdges();

        List<WorkflowNode> workflowNodes = new ArrayList<>();
        for (WorkflowNodeDto nodeDto : nodeDtos) {
            WorkflowNode node = new WorkflowNode();
            node.setNodeId(nodeDto.getId());
            node.setName(nodeDto.getName());
            node.setNodeType(nodeDto.getType());
            node.setWorkflow(workflow);
            node.setStyle(nodeDto.getStyle());
            workflowNodes.add(node);
        }

        List<WorkflowEdge> workflowEdges = new ArrayList<>();
        for (WorkflowEdgeDto edgeDto : edgeDtos) {
            WorkflowEdge edge = new WorkflowEdge();
            edge.setEdgeId(edgeDto.getId());

            WorkflowNode sourceNode = workflowNodes.stream()
                    .filter(node -> node.getNodeId().equals(edgeDto.getSource()))
                    .findFirst()
                    .orElse(null);
            WorkflowNode targetNode = workflowNodes.stream()
                    .filter(node -> node.getNodeId().equals(edgeDto.getTarget()))
                    .findFirst()
                    .orElse(null);

            edge.setSource(sourceNode);
            edge.setTarget(targetNode);
            edge.setTargetHandle(edgeDto.getTargetHandle());
            edge.setSourceHandle(edgeDto.getSourceHandle());
            edge.setWorkflow(workflow);
            workflowEdges.add(edge);
        }
        workflow.setNodes(workflowNodes);
        workflow.setEdges(workflowEdges);
        this.create(workflow);
        return StandardResponse.success("Workflow created successfully!");
    }

    @Override
    public StandardResponse<?> getWorkflows() throws Exception {
        return StandardResponse.success("");
    }

    @Override
    public StandardResponse<?> getWorkflowById(Long id) throws Exception {
        Workflow workflow = this.findById(id);

        ModelMapper modelMapper = new ModelMapper();
        WorkflowDto workflowDto = modelMapper.map(workflow, WorkflowDto.class);

        List<WorkflowNode> nodes = workflow.getNodes();
        List<WorkflowNodeDto> nodeDtos = nodes.stream()
                .map(node -> modelMapper.map(node, WorkflowNodeDto.class))
                .collect(Collectors.toList());

        List<WorkflowEdge> edges = workflow.getEdges();
        List<WorkflowEdgeDto> edgeDtos = edges.stream()
                .map(edge -> {
                    WorkflowEdgeDto edgeDto = modelMapper.map(edge, WorkflowEdgeDto.class);
                    edgeDto.setSource(edge.getSource().getNodeId());
                    edgeDto.setTarget(edge.getTarget().getNodeId());
                    return edgeDto;
                })
                .collect(Collectors.toList());

        WorkflowStageDto stages = new WorkflowStageDto();
        stages.setNodes(nodeDtos);
        stages.setEdges(edgeDtos);

        workflowDto.setStages(stages);

        return StandardResponse.success(workflowDto);
    }
}
