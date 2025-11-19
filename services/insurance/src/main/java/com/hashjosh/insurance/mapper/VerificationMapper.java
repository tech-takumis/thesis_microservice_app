package com.hashjosh.insurance.mapper;

import com.hashjosh.insurance.clients.DocumentServiceClient;
import com.hashjosh.insurance.dto.verification.VerificationRequest;
import com.hashjosh.insurance.dto.verification.VerificationResponse;
import com.hashjosh.insurance.entity.Insurance;
import com.hashjosh.insurance.entity.Verification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VerificationMapper {

    private final DocumentServiceClient documentServiceClient;

    public Verification toEntity(VerificationRequest request, Insurance insurance, UUID verifierId, String verifierName) {
        return Verification.builder()
                .verifierId(verifierId)
                .verifierName(verifierName)
                .remarks(request.getRemarks())
                .fieldValues(request.getFieldValues())
                .verificationDocuments(request.getVerificationDocuments())
                .verifiedAt(LocalDateTime.now())
                .insurance(insurance)
                .build();
    }

    public Verification toEntity(VerificationRequest request, Insurance insurance, UUID verifierId, String verifierName, List<UUID> documentIds) {
        return Verification.builder()
                .verifierId(verifierId)
                .verifierName(verifierName)
                .remarks(request.getRemarks())
                .fieldValues(request.getFieldValues())
                .verificationDocuments(documentIds)
                .verifiedAt(LocalDateTime.now())
                .insurance(insurance)
                .build();
    }

    public VerificationResponse toResponse(Verification verification) {

        List<String> getPresignedUrls = verification.getVerificationDocuments().stream()
                .map(documentId -> getPresignedUrl(verification.getVerifierId(), documentId, 60))
                .toList();

        return VerificationResponse.builder()
                .id(verification.getId())
                .insuranceId(verification.getInsurance().getId())
                .verifierId(verification.getVerifierId())
                .verifierName(verification.getVerifierName())
                .remarks(verification.getRemarks())
                .fieldValues(verification.getFieldValues())
                .verificationDocuments(getPresignedUrls)
                .verifiedAt(verification.getVerifiedAt())
                .build();
    }

    public void updateEntity(Verification verification, VerificationRequest request, UUID verifierId, String verifierName) {
        verification.setVerifierId(verifierId);
        verification.setVerifierName(verifierName);
        verification.setRemarks(request.getRemarks());
        verification.setFieldValues(request.getFieldValues());
        if (request.getVerificationDocuments() != null) {
            verification.setVerificationDocuments(request.getVerificationDocuments());
        }
        verification.setVerifiedAt(LocalDateTime.now());
    }

    public void updateEntity(Verification verification, VerificationRequest request, UUID verifierId, String verifierName, List<UUID> documentIds) {
        verification.setVerifierId(verifierId);
        verification.setVerifierName(verifierName);
        verification.setRemarks(request.getRemarks());
        verification.setFieldValues(request.getFieldValues());
        verification.setVerificationDocuments(documentIds);
        verification.setVerifiedAt(LocalDateTime.now());
    }

    private String getPresignedUrl(UUID userId,UUID documentId, int expiryMinutes) {
        return documentServiceClient.generatePresignedUrl(userId, documentId, expiryMinutes);
    }

}
