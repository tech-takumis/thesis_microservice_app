package com.hashjosh.insurance.dto.insurance;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceResponse {
    private UUID insuranceId;
    private UUID farmerId;
    private UUID verificationId;
    private UUID policyId;
    private UUID inspectionId;
    private UUID claimId;
    private String farmerName;
    private UUID submissionId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
