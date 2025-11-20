package com.hashjosh.application.mapper;

import com.hashjosh.application.dto.workflow.ApplicationWorkflowResponse;
import com.hashjosh.application.model.ApplicationWorkflow;
import org.springframework.stereotype.Component;

@Component
public class WorkflowMapper {
    public ApplicationWorkflowResponse toWorkflowResponse(ApplicationWorkflow workflow) {
        return ApplicationWorkflowResponse.builder()
                .id(workflow.getId())
                .claimEnabled(workflow.isClaimEnabled())
                .verificationEnabled(workflow.isVerificationEnabled())
                .inspectionEnabled(workflow.isInspectionEnabled())
                .policyEnabled(workflow.isPolicyEnabled())
                .createdAt(workflow.getCreatedAt())
                .updatedAt(workflow.getUpdatedAt())
                .build();
    }
}
