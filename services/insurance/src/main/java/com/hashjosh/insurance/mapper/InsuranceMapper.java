package com.hashjosh.insurance.mapper;

import com.hashjosh.insurance.dto.insurance.InsuranceResponse;
import com.hashjosh.insurance.entity.Insurance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InsuranceMapper {

    public InsuranceResponse toInsuranceResponse(
            Insurance insurance
    ) {
        return InsuranceResponse.builder()
                .insuranceId(insurance.getId())
                .farmerId(insurance.getFarmerId())
                .farmerName(insurance.getFarmerName())
                .submissionId(insurance.getSubmissionId())
                .verificationId(insurance.getVerification() != null ? insurance.getVerification().getId() : null)
                .policyId(insurance.getPolicy() != null ? insurance.getPolicy().getId() : null)
                .inspectionId(insurance.getInspection() != null ? insurance.getInspection().getId() : null)
                .claimId(insurance.getClaim() != null ? insurance.getClaim().getId() : null)
                .status(insurance.getCurrentStatus().name())
                .createdAt(insurance.getCreatedAt())
                .updatedAt(insurance.getUpdatedAt())
                .build();

    }

}
