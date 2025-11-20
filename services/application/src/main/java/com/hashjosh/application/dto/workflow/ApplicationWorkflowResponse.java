package com.hashjosh.application.dto.workflow;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationWorkflowResponse {
    private UUID id;

    private boolean verificationEnabled;

    private boolean inspectionEnabled;
    private boolean policyEnabled ;
    private boolean claimEnabled;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
