package com.hashjosh.insurance.mapper;

import com.hashjosh.insurance.clients.DocumentServiceClient;
import com.hashjosh.insurance.dto.claim.ClaimResponse;
import com.hashjosh.insurance.entity.Claim;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClaimMapper {

    private final DocumentServiceClient documentServiceClient;
    public ClaimResponse toResponse(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .insuranceId(claim.getInsurance().getId())
                .filedAt(claim.getFiledAt())
                .damageAssessment(claim.getDamageAssessment())
                .claimAmount(claim.getClaimAmount())
                .supportingFiles(getPresignedUrl(claim))
                .fieldValues(claim.getFieldValues())
                .build();
    }

    private List<String> getPresignedUrl(Claim claim) {

        return claim.getSupportingFiles().stream()
                .map(documentId -> documentServiceClient.generatePresignedUrl(claim.getInsurance().getFarmerId(), documentId, 60))
                .toList();
    }
}
