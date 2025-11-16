package com.hashjosh.insurance.dto.insurance;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hashjosh.constant.ai.AIResultDTO;
import com.hashjosh.constant.application.ApplicationResponseDto;
import com.hashjosh.constant.farmer.FarmerReponse;
import com.hashjosh.insurance.dto.batch.BatchResponseDTO;
import com.hashjosh.insurance.dto.claim.ClaimResponse;
import com.hashjosh.insurance.dto.inspection.InspectionResponse;
import com.hashjosh.insurance.dto.policy.PolicyResponse;
import com.hashjosh.insurance.dto.verification.VerificationResponse;
import com.hashjosh.insurance.entity.Inspection;
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
    private String farmerName;
    private String policyNumber;
    private String status;
    private Double claimAmount;
    private Boolean isVerified;
    private String rejectionReason;
    private Boolean requiredAIProcessing;
    private BatchResponseDTO batch;
    private ApplicationResponseDto application;
    private AIResultDTO aiResult;
    private ClaimResponse claim;
    private VerificationResponse verification;
    private InspectionResponse inspection;
    private PolicyResponse policy;
    private FarmerReponse farmer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
