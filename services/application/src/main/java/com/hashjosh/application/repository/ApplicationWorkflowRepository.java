package com.hashjosh.application.repository;

import com.hashjosh.application.model.ApplicationWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApplicationWorkflowRepository extends JpaRepository<ApplicationWorkflow, UUID> {
}
