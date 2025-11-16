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

    public ClaimResponse toResponse(Claim claim, List<String> documentUrls) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .insuranceId(claim.getInsurance().getId())
                .filedAt(claim.getFiledAt())
                .damageAssessment(claim.getDamageAssessment())
                .claimAmount(claim.getClaimAmount())
                .supportingFiles(documentUrls)
                .fieldValues(claim.getFieldValues())
                .build();
    }
}
