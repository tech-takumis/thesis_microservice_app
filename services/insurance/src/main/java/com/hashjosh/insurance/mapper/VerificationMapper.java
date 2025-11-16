package com.hashjosh.insurance.mapper;

import com.hashjosh.insurance.dto.verification.VerificationRequest;
import com.hashjosh.insurance.dto.verification.VerificationResponse;
import com.hashjosh.insurance.entity.Insurance;
import com.hashjosh.insurance.entity.Verification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class VerificationMapper {

    public Verification toEntity(VerificationRequest request, Insurance insurance, UUID verifierId, String verifierName) {
        return Verification.builder()
                .verifierId(verifierId)
                .verifierName(verifierName)
                .remarks(request.getRemarks())
                .fieldValues(request.getFieldValues())
                .verifiedAt(LocalDateTime.now())
                .insurance(insurance)
                .build();
    }

    public VerificationResponse toResponse(Verification verification) {
        return VerificationResponse.builder()
                .id(verification.getId())
                .insuranceId(verification.getInsurance().getId())
                .verifierId(verification.getVerifierId())
                .verifierName(verification.getVerifierName())
                .remarks(verification.getRemarks())
                .fieldValues(verification.getFieldValues())
                .verifiedAt(verification.getVerifiedAt())
                .build();
    }

    public void updateEntity(Verification verification, VerificationRequest request, UUID verifierId, String verifierName) {
        verification.setVerifierId(verifierId);
        verification.setVerifierName(verifierName);
        verification.setRemarks(request.getRemarks());
        verification.setFieldValues(request.getFieldValues());
        verification.setVerifiedAt(LocalDateTime.now());
    }

}
