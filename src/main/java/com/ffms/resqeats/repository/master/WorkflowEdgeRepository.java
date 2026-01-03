package com.ffms.resqeats.repository.master;

import com.ffms.resqeats.models.master.WorkflowEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowEdgeRepository extends JpaRepository<WorkflowEdge, Long> {
    Optional<WorkflowEdge> findById(Integer id);
}
