package com.hashjosh.constant.workflow;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationWorkflowResponse {
    private UUID id;

    private boolean verification_enabled;

    private boolean inspection_enabled;
    private boolean policy_enabled ;
    private boolean claim_enabled;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
