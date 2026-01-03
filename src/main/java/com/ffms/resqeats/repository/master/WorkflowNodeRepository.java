package com.ffms.resqeats.repository.master;

import com.ffms.resqeats.models.master.WorkflowNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowNodeRepository extends JpaRepository<WorkflowNode, Long> {
    Optional<WorkflowNode> findById(Integer id);
}
