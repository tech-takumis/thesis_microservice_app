package com.hashjosh.insurance.mapper;

import com.hashjosh.constant.ai.AIResultDTO;
import com.hashjosh.constant.application.ApplicationResponseDto;
import com.hashjosh.constant.farmer.FarmerReponse;
import com.hashjosh.insurance.clients.AIClient;
import com.hashjosh.insurance.clients.ApplicationClient;
import com.hashjosh.insurance.clients.DocumentServiceClient;
import com.hashjosh.insurance.clients.FarmerClient;
import com.hashjosh.insurance.dto.InsuranceFilter;
import com.hashjosh.insurance.dto.batch.BatchResponseDTO;
import com.hashjosh.insurance.dto.claim.ClaimResponse;
import com.hashjosh.insurance.dto.inspection.InspectionResponse;
import com.hashjosh.insurance.dto.insurance.InsuranceResponse;
import com.hashjosh.insurance.dto.policy.PolicyResponse;
import com.hashjosh.insurance.dto.verification.VerificationResponse;
import com.hashjosh.insurance.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InsuranceMapper {

    private final ApplicationClient applicationClient;
    private final FarmerClient farmerClient;
    private final AIClient aiClient;
    private final DocumentServiceClient documentClient;

    public InsuranceResponse toInsuranceResponse(
            Insurance insurance,
           InsuranceFilter filter
    ) {
        InsuranceResponse.InsuranceResponseBuilder builder = InsuranceResponse.builder()
                .insuranceId(insurance.getId())
                .createdAt(insurance.getCreatedAt())
                .updatedAt(insurance.getUpdatedAt());

        // Conditionally include batch information
        if (Boolean.TRUE.equals(filter.getBatch()) && insurance.getBatch() != null) {
            BatchResponseDTO batch = mapBatchToBasicResponse(insurance.getBatch());
            builder.batch(batch);
        }

        // Conditionally include application information
        if (Boolean.TRUE.equals(filter.getApplication()) && insurance.getSubmissionId() != null) {
            try {
                ApplicationResponseDto application = getApplication(
                    insurance.getSubmissionId(),
                    String.valueOf(insurance.getFarmerId())
                );
                builder.application(application);
            } catch (Exception e) {
                // Log error but continue without application data
                log.warn("Failed to fetch application data for submission ID {}: {}",
                        insurance.getSubmissionId(), e.getMessage(), e);
            }
        }

        // Conditionally include farmer information
        if (Boolean.TRUE.equals(filter.getFarmer()) && insurance.getFarmerId() != null) {
            try {
                FarmerReponse farmer = getFarmer(String.valueOf(insurance.getFarmerId()));
                builder.farmer(farmer);
            } catch (Exception e) {
                // Log error but continue without farmer data
                log.warn("Failed to fetch farmer data for user {}: {}",
                        insurance.getFarmerId(), e.getMessage(), e);
            }
        }

        // Conditionally include AI processing information
        if (insurance.getIsAIProcessed() != null && insurance.getIsAIProcessed().equals(Boolean.TRUE)) {
            builder.requiredAIProcessing(insurance.getIsAIProcessed());
            try {
                AIResultDTO aiResult = getAIResultByInsuranceId(insurance.getSubmissionId().toString());
                builder.aiResult(aiResult);
            } catch (Exception e) {
                // Log error but continue without AI result data
                log.warn("Failed to fetch AI result data for submission ID {}: {}",
                        insurance.getSubmissionId(), e.getMessage(), e);
            }
        }

        if(filter.getVerification() != null && filter.getVerification().equals(Boolean.TRUE)){
            Verification verification = insurance.getVerification();
            builder.verification(verification != null ? mapVerificationToBasicResponse(verification) : null);
        }

        if(filter.getInspection() != null && filter.getInspection().equals(Boolean.TRUE)){
            Inspection inspection = insurance.getInspection();
            builder.inspection(inspection != null ? mapInspectionToBasicResponse(inspection) : null);
        }

        if(filter.getPolicy() != null && filter.getPolicy().equals(Boolean.TRUE)){
            Policy policy = insurance.getPolicy();
            builder.policy(policy != null ? getPolicyToBasicResponse(policy) : null);
        }

        if(filter.getClaim() != null && filter.getClaim().equals(Boolean.TRUE)){
            Claim claim = insurance.getClaim();
            builder.claim(claim != null ? getClaimToBasicResponse(claim) : null);
        }

        return builder.build();
    }

    private ApplicationResponseDto getApplication(UUID submissionId, String userId){
        return  applicationClient.getApplicationById(submissionId, userId);
    }

    private FarmerReponse getFarmer(String userId){
        return farmerClient.getFarmerById(userId, userId);
    }

    // Simple mapping to avoid circular dependency with BatchMapper
    private BatchResponseDTO mapBatchToBasicResponse(Batch batch) {
        return BatchResponseDTO.builder()
                .id(batch.getId())
                .batchName(batch.getName())
                .description(batch.getDescription())
                .maxApplications(batch.getMaxApplications())
                .totalApplications(batch.getTotalApplications())
                .isAvailable(batch.isAvailable())
                .startDate(batch.getStartDate())
                .endDate(batch.getEndDate())
                .build();
    }

    private AIResultDTO getAIResultByInsuranceId(String submissionId) {
        return aiClient.getAIResultByApplicationId(submissionId);
    }

    private VerificationResponse mapVerificationToBasicResponse(Verification verification) {
        return VerificationResponse.builder()
                .id(verification.getId())
                .insuranceId(verification.getInsurance().getId())
                .remarks(verification.getRemarks())
                .verifiedAt(verification.getVerifiedAt())
                .verifiedBy(verification.getVerifierId())
                .build();
    }


    private InspectionResponse mapInspectionToBasicResponse(Inspection inspection) {

        List<String> photos = new ArrayList<>();
        if(inspection.getPhotos() != null && !inspection.getPhotos().isEmpty()){
             photos = inspection.getPhotos().stream()
                    .map(photo -> getPresignedUrl( String.valueOf(inspection.getInspectorId()),
                            photo,
                            60)
                    )
                    .toList();
        }
        return InspectionResponse.builder()
                .id(inspection.getId())
                .inspectedAt(inspection.getInspectedAt())
                .inspectorId(inspection.getInspectorId())
                .inspectorName(inspection.getInspectorName())
                .fieldValues(inspection.getFieldValues())
                .photos(photos)
                .build();
    }

    private PolicyResponse getPolicyToBasicResponse(Policy policy){
        // Policy mapping logic can be implemented here
        return PolicyResponse.builder()
                .id(policy.getId())
                .insuranceId(policy.getInsurance().getId())
                .policyNumber(policy.getPolicyNumber())
                .effectiveDate(policy.getEffectiveDate())
                .expiryDate(policy.getExpiryDate())
                .build();
    }

    private ClaimResponse getClaimToBasicResponse(Claim claim){

        List<String> photos = new ArrayList<>();

        if(claim.getSupportingFiles() != null && !claim.getSupportingFiles().isEmpty()){
            photos = claim.getSupportingFiles().stream()
                    .map(photo -> getPresignedUrl( String.valueOf(claim.getInsurance().getFarmerId()),
                            photo,
                            60)
                    )
                    .toList();
        }

        return ClaimResponse.builder()
                .id(claim.getId())
                .insuranceId(claim.getInsurance().getId())
                .filedAt(claim.getFiledAt())
                .damageAssessment(claim.getDamageAssessment())
                .claimAmount(claim.getClaimAmount())
                .supportingFiles(photos)
                .fieldValues(claim.getFieldValues())
                .build();
    }

    private String getPresignedUrl(String userId,UUID documentId, int expiryInMinutes) {
        return documentClient.generatePresignedUrl(userId, documentId, expiryInMinutes);
    }
}
