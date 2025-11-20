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

    private final PolicyMapper policyMapper;
    private final ClaimMapper claimMapper;
    private final InspectionMapper inspectionMapper;
    private final VerificationMapper verificationMapper;
    private final BatchMapper batchMapper;

    public InsuranceResponse toInsuranceResponse(
            Insurance insurance
    ) {
        return InsuranceResponse.builder()
                .insuranceId(insurance.getId())
                .submissionId(insurance.getSubmissionId())
                .applicationName(insurance.getApplicationTypeName())
                .batch(batchMapper.toBatchResponseDTO(insurance.getBatch()))
                .policy(insurance.getPolicy() != null ? policyMapper.toResponse(insurance.getPolicy()) : null)
                .claim(insurance.getClaim() != null ? claimMapper.toResponse(insurance.getClaim()) : null)
                .inspection(insurance.getInspection() != null ? inspectionMapper.toResponse(insurance.getInspection()) : null)
                .verification(insurance.getVerification() != null ? verificationMapper.toResponse(insurance.getVerification()) : null)
                .status(insurance.getCurrentStatus().name())
                .build();

    }

}
