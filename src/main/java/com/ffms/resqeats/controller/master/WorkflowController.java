package com.ffms.resqeats.controller.master;

import com.ffms.resqeats.dto.master.workflow.WorkflowDto;
import com.ffms.resqeats.service.master.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Workflow management controller for CRUD operations on workflows.
 */
@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    /**
     * Create a new workflow
     */
    @PostMapping
    @PreAuthorize("hasPermission(#id, @appUtils.workflowResource, @appUtils.writePrivilege)")
    public ResponseEntity<?> createWorkflow(@Valid @RequestBody WorkflowDto workflowDto) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED).body(workflowService.createWorkflow(workflowDto));
    }

    /**
     * Get all workflows
     */
    @GetMapping
    @PreAuthorize("hasPermission(#id, @appUtils.workflowResource, @appUtils.readPrivilege)")
    public ResponseEntity<?> getWorkflows() throws Exception {
        return ResponseEntity.ok(workflowService.getWorkflows());
    }

    /**
     * Get workflow by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(#id, @appUtils.workflowResource, @appUtils.readPrivilege)")
    public ResponseEntity<?> getWorkflowById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(workflowService.getWorkflowById(id));
    }
}
