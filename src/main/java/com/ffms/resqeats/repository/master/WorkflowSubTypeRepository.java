package com.ffms.resqeats.repository.master;

import com.ffms.resqeats.models.master.WorkflowSubType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowSubTypeRepository extends JpaRepository<WorkflowSubType, Long> {
    Optional<WorkflowSubType> findById(Integer id);
}
