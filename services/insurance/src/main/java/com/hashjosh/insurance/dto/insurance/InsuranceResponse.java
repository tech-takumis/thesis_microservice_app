package com.hashjosh.insurance.dto.insurance;

import com.hashjosh.insurance.dto.batch.BatchResponseDTO;
import com.hashjosh.insurance.dto.claim.ClaimResponse;
import com.hashjosh.insurance.dto.inspection.InspectionResponse;
import com.hashjosh.constant.policy.PolicyResponse;
import com.hashjosh.insurance.dto.verification.VerificationResponse;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceResponse {
    private UUID insuranceId;
    private UUID submissionId;
    private String applicationName;
    private String status;
    private BatchResponseDTO batch;
    private VerificationResponse verification;
    private InspectionResponse inspection;
    private ClaimResponse claim;
    private PolicyResponse policy;
}
