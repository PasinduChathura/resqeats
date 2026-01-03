package com.ffms.trackable.repository.master;

import com.ffms.trackable.models.master.WorkflowSubType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowSubTypeRepository extends JpaRepository<WorkflowSubType, Long> {
    Optional<WorkflowSubType> findById(Integer id);
}
