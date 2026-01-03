package com.ffms.trackable.repository.master;

import com.ffms.trackable.models.master.WorkflowNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowNodeRepository extends JpaRepository<WorkflowNode, Long> {
    Optional<WorkflowNode> findById(Integer id);
}
