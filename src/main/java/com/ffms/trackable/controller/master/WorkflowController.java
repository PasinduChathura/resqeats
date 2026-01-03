package com.ffms.trackable.controller.master;

import com.ffms.trackable.dto.master.workflow.WorkflowDto;
import com.ffms.trackable.service.master.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/workflow")
public class WorkflowController {
    @Autowired
    WorkflowService workflowService;

    @PostMapping("/")
    @PreAuthorize("hasPermission(#id, 'workflow', 'write')")
    public ResponseEntity<?> createWorkflow(@Valid @RequestBody WorkflowDto workflowDto) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(workflowService.createWorkflow(workflowDto));
    }

    @GetMapping("/")
    @PreAuthorize("hasPermission(#id, 'workflow', 'read')")
    public ResponseEntity<?> getWorkflows() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(workflowService.getWorkflows());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'workflow', 'read')")
    public ResponseEntity<?> getWorkflowById(@PathVariable Long id) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(workflowService.getWorkflowById(id));
    }

}
