package com.hashjosh.insurance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashjosh.constant.ai.AIResultDTO;
import com.hashjosh.constant.document.dto.DocumentResponse;
import com.hashjosh.constant.pcic.enums.InsuranceStatus;
import com.hashjosh.insurance.clients.AIClient;
import com.hashjosh.insurance.clients.DocumentServiceClient;
import com.hashjosh.insurance.dto.claim.ClaimAIRequest;
import com.hashjosh.insurance.dto.claim.ClaimRequest;
import com.hashjosh.insurance.dto.claim.ClaimResponse;
import com.hashjosh.insurance.dto.claim.ClaimUpdateRequest;
import com.hashjosh.insurance.entity.Claim;
import com.hashjosh.insurance.entity.Insurance;
import com.hashjosh.insurance.exception.ApiException;
import com.hashjosh.insurance.kafka.KafkaProducer;
import com.hashjosh.insurance.mapper.ClaimMapper;
import com.hashjosh.insurance.repository.ClaimRepository;
import com.hashjosh.insurance.repository.InsuranceRepository;
import com.hashjosh.kafkacommon.application.ClaimProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimService {
    private final ClaimRepository claimRepository;
    private final ClaimMapper claimMapper;
    private final InsuranceRepository insuranceRepository;
    private final AIClient aiClient;
    private final DocumentServiceClient documentServiceClient;
    private final ObjectMapper objectMapper;
    private final KafkaProducer kafkaProducer;

    @Transactional
    public ClaimResponse createClaimManually(ClaimRequest request, List<MultipartFile> supportingFiles, String userId) {
        log.info("Creating claim manually for insurance ID: {}", request.getInsuranceId());

        Insurance insurance = getInsuranceById(request.getInsuranceId());
        validateInsuranceForClaim(insurance);

        List<UUID> documentIds = uploadSupportingFiles(supportingFiles, userId);

        Double claimAmount = calculateClaimAmount(insurance, request.getFieldValues());

        Claim claim = Claim.builder()
                .insurance(insurance)
                .filedAt(LocalDateTime.now())
                .damageAssessment(request.getDamageAssessment())
                .claimAmount(claimAmount)
                .isFinalized(request.isFinalized())
                .supportingFiles(documentIds)
                .fieldValues(request.getFieldValues())
                .build();

        claim = claimRepository.save(claim);
        log.info("Claim created successfully with ID: {}", claim.getId());
        insurance.setCurrentStatus(InsuranceStatus.CLAIMED_ISSUED);
        insuranceRepository.save(insurance);

        if(request.isFinalized()){
            ClaimProcessedEvent event = ClaimProcessedEvent.builder()
                    .submissionId(insurance.getSubmissionId())
                    .userId(insurance.getFarmerId())
                    .claimId(claim.getId())
                    .claimAmount(claimAmount)
                    .payoutStatus("FINALIZED")
                    .processedAt(LocalDateTime.now())
                    .build();

            kafkaProducer.publishEvent("application-claim",event);
        }

        return claimMapper.toResponse(claim);
    }

    @Transactional
    public ClaimResponse createClaimFromAI(ClaimAIRequest request, List<MultipartFile> supportingFiles, String userId) {
        log.info("Creating claim from AI for insurance ID: {} with application ID: {}",
                request.getInsuranceId(), request.getApplicationId());

        Insurance insurance = getInsuranceById(request.getInsuranceId());
        validateInsuranceForClaim(insurance);

        AIResultDTO aiResult = aiClient.getAIResultByApplicationId(request.getApplicationId());

        List<UUID> documentIds = uploadSupportingFiles(supportingFiles, userId);

        JsonNode aiFieldValues = createAIFieldValues(aiResult);
        Double claimAmount = calculateClaimAmountFromAI(insurance, aiResult);

        Claim claim = Claim.builder()
                .insurance(insurance)
                .filedAt(LocalDateTime.now())
                .damageAssessment(aiResult.getPrediction())
                .claimAmount(claimAmount)
                .isFinalized(false)
                .supportingFiles(documentIds)
                .fieldValues(aiFieldValues)
                .build();

        claim = claimRepository.save(claim);
        log.info("Claim created from AI successfully with ID: {}", claim.getId());

        insurance.setCurrentStatus(InsuranceStatus.CLAIMED_ISSUED);
        insuranceRepository.save(insurance);

        return claimMapper.toResponse(claim);
    }

    public ClaimResponse getClaimById(UUID claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> ApiException.notFound("Claim not found with ID: " + claimId));

        return claimMapper.toResponse(claim);
    }

    public List<ClaimResponse> getAllClaims() {
        List<Claim> claims = claimRepository.findAll();
        return claims.stream()
                .map(claimMapper::toResponse)
                .toList();
    }

    public ClaimResponse getClaimsByInsuranceId(UUID insuranceId) {
        Claim claims = claimRepository.findByInsuranceId(insuranceId)
                .orElseThrow(() -> ApiException.notFound("Claim not found for insurance ID: " + insuranceId));
        return claimMapper.toResponse(claims);
    }

    @Transactional
    public ClaimResponse updateClaim(UUID claimId, ClaimUpdateRequest request, List<MultipartFile> supportingFiles, String userId) {
        Claim existingClaim = claimRepository.findById(claimId)
                .orElseThrow(() -> ApiException.notFound("Claim not found with ID: " + claimId));

        log.info("Updating claim ID: {} - claimAmount: {}, damageAssessment: {}, supportingFiles count: {}",
                claimId, request.getClaimAmount(),
                request.getDamageAssessment() != null ? "provided" : "not provided",
                supportingFiles != null ? supportingFiles.size() : 0);

        boolean hasUpdates = false;

        // Update claim amount if provided
        if (request.getClaimAmount() != null) {
            if (request.getClaimAmount() <= 0) {
                throw ApiException.badRequest("Claim amount must be greater than 0");
            }
            existingClaim.setClaimAmount(request.getClaimAmount());
            log.info("Updated claim amount from {} to {}", existingClaim.getClaimAmount(), request.getClaimAmount());
            hasUpdates = true;
        }

        // Update damage assessment if provided
        if (request.getDamageAssessment() != null && !request.getDamageAssessment().trim().isEmpty()) {
            existingClaim.setDamageAssessment(request.getDamageAssessment().trim());
            log.info("Updated damage assessment for claim ID: {}", claimId);
            hasUpdates = true;
        }

        // Upload and add new supporting files if provided
        if (supportingFiles != null && !supportingFiles.isEmpty()) {
            List<UUID> newDocumentIds = uploadSupportingFiles(supportingFiles, userId);

            // Get existing supporting files or initialize empty list
            List<UUID> allDocumentIds = existingClaim.getSupportingFiles() != null
                ? new ArrayList<>(existingClaim.getSupportingFiles())
                : new ArrayList<>();

            // Add new document IDs
            allDocumentIds.addAll(newDocumentIds);
            existingClaim.setSupportingFiles(allDocumentIds);

            log.info("Added {} new supporting files. Total files: {}",
                    newDocumentIds.size(), allDocumentIds.size());
            hasUpdates = true;
        }

        if(request.getIsFinalized() != null && !existingClaim.isFinalized()) {
            existingClaim.setFinalized(request.getIsFinalized());
            log.info("Updated isFinalized to {}", request.getIsFinalized());

            ClaimProcessedEvent event = ClaimProcessedEvent.builder()
                    .submissionId(existingClaim.getInsurance().getSubmissionId())
                    .userId(existingClaim.getInsurance().getFarmerId())
                    .claimId(existingClaim.getId())
                    .payoutStatus("FINALIZED")
                    .claimAmount(request.getClaimAmount() != null ? request.getClaimAmount() : existingClaim.getClaimAmount())
                    .processedAt(LocalDateTime.now())
                    .build();

            kafkaProducer.publishEvent("application-claim",event);

            hasUpdates = true;
        }

        if (!hasUpdates) {
            log.warn("No updates provided for claim ID: {}", claimId);
            throw ApiException.badRequest("No valid updates provided. Please provide claim amount, damage assessment, or supporting files.");
        }

        existingClaim = claimRepository.save(existingClaim);
        log.info("Claim updated successfully with ID: {}", claimId);

        return claimMapper.toResponse(existingClaim);
    }

    @Transactional
    public void deleteClaim(UUID claimId) {
        if (!claimRepository.existsById(claimId)) {
            throw ApiException.notFound("Claim not found with ID: " + claimId);
        }

        claimRepository.deleteById(claimId);
        log.info("Claim deleted successfully with ID: {}", claimId);
    }

    private Insurance getInsuranceById(UUID insuranceId) {
        return insuranceRepository.findById(insuranceId)
                .orElseThrow(() -> ApiException.notFound("Insurance not found with ID: " + insuranceId));
    }

    private void validateInsuranceForClaim(Insurance insurance) {
        if (insurance.getClaim() != null) {
            throw ApiException.conflict("Claim already exists for insurance ID: " + insurance.getId());
        }

        // Check if insurance has inspection data with COC/Policy number
        if (insurance.getInspection() == null || insurance.getInspection().getFieldValues() == null) {
            throw ApiException.badRequest("Insurance must have inspection data before creating a claim");
        }

        // Check for COC/Policy number in inspection field values
        JsonNode inspectionValues = insurance.getInspection().getFieldValues();
        String cocNumber = getCocNumberFromInspection(inspectionValues);

        if (cocNumber == null || cocNumber.trim().isEmpty()) {
            throw ApiException.badRequest("Insurance must have a COC/Policy number in inspection data before creating a claim");
        }

        log.info("Validated insurance with COC/Policy number: {}", cocNumber);
    }

    private List<UUID> uploadSupportingFiles(List<MultipartFile> files, String userId) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        List<UUID> documentIds = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                DocumentResponse documentResponse = documentServiceClient.uploadDocument(file, userId);
                documentIds.add(documentResponse.getDocumentId());
            }
        }
        return documentIds;
    }

    private Double calculateClaimAmount(Insurance insurance, JsonNode fieldValues) {
        try {
            if (insurance.getInspection() == null || insurance.getInspection().getFieldValues() == null) {
                throw ApiException.badRequest("Insurance inspection data is required for claim calculation");
            }

            JsonNode inspectionValues = insurance.getInspection().getFieldValues();

            // Get area values from inspection
            double areaInsured = getDoubleValue(inspectionValues, "area_insured");
            double areaDamaged = getDoubleValue(inspectionValues, "area_damaged");

            // Calculate yield loss percentage based on damaged area
            double yieldLossPercentage = areaDamaged / areaInsured;

            // Get crop type and determine coverage amount per hectare based on PCIC rates
            String cropType = getStringValue(inspectionValues, "insured_crops", "Rice"); // Default to Rice
            String varietyPlanted = getStringValue(inspectionValues, "variety_planted", "Inbred"); // Default to Inbred

            // Calculate amount of cover based on PCIC rates per hectare
            double coveragePerHectare = getCoveragePerHectare(cropType, varietyPlanted);
            double totalAmountOfCover = areaInsured * coveragePerHectare;

            // Get cultivation stage adjustment factor
            double stageAdjustmentFactor = getStageAdjustmentFactor(inspectionValues);

            // PCIC Formula: Indemnity = (Amount of Cover × % Yield Loss × Adjustment Factor based on crop stage)
            double claimAmount = totalAmountOfCover * yieldLossPercentage * stageAdjustmentFactor;

            // Validate claim amount
            if (claimAmount < 0) {
                throw ApiException.badRequest("Calculated claim amount cannot be negative");
            }

            if (claimAmount > totalAmountOfCover) {
                log.warn("Calculated claim amount ({}) exceeds total coverage ({}). Capping at total coverage.",
                        claimAmount, totalAmountOfCover);
                claimAmount = totalAmountOfCover;
            }

            log.info("PCIC Manual Claim Calculation - Crop: {}, Variety: {}, Area: {} ha, Damaged: {} ha, Yield Loss: {}%, Coverage/ha: ₱{}, Total Cover: ₱{}, Stage Adjustment: {}%, Final Claim: ₱{}",
                    cropType, varietyPlanted, areaInsured, areaDamaged, yieldLossPercentage * 100, coveragePerHectare, totalAmountOfCover, stageAdjustmentFactor * 100, claimAmount);

            return claimAmount;

        } catch (Exception e) {
            log.error("Error calculating claim amount", e);
            throw ApiException.badRequest("Failed to calculate claim amount: " + e.getMessage());
        }
    }

    private Double calculateClaimAmountFromAI(Insurance insurance, AIResultDTO aiResult) {
        try {
            if (insurance.getInspection() == null || insurance.getInspection().getFieldValues() == null) {
                throw ApiException.badRequest("Insurance inspection data is required for claim calculation");
            }

            JsonNode inspectionValues = insurance.getInspection().getFieldValues();

            // Get area values from inspection
            double areaInsured = getDoubleValue(inspectionValues, "area_insured");

            // AI severity percentage represents the yield loss percentage
            double yieldLossPercentage = aiResult.getSeverity() / 100.0; // Convert percentage to decimal

            // Get crop type and determine coverage amount per hectare based on PCIC rates
            String cropType = getStringValue(inspectionValues, "insured_crops", "Rice"); // Default to Rice
            String varietyPlanted = getStringValue(inspectionValues, "variety_planted", "Inbred"); // Default to Inbred

            // Calculate amount of cover based on PCIC rates per hectare
            double coveragePerHectare = getCoveragePerHectare(cropType, varietyPlanted);
            double totalAmountOfCover = areaInsured * coveragePerHectare;

            // Get cultivation stage adjustment factor
            double stageAdjustmentFactor = getStageAdjustmentFactor(inspectionValues);

            // PCIC Formula: Indemnity = (Amount of Cover × % Yield Loss × Adjustment Factor based on crop stage)
            double claimAmount = totalAmountOfCover * yieldLossPercentage * stageAdjustmentFactor;

            // Validate claim amount
            if (claimAmount < 0) {
                throw ApiException.badRequest("Calculated claim amount cannot be negative");
            }

            if (claimAmount > totalAmountOfCover) {
                log.warn("Calculated claim amount ({}) exceeds total coverage ({}). Capping at total coverage.",
                        claimAmount, totalAmountOfCover);
                claimAmount = totalAmountOfCover;
            }

            log.info("PCIC AI Claim Calculation - Crop: {}, Variety: {}, Area: {} ha, Severity: {}%, Coverage/ha: ₱{}, Total Cover: ₱{}, Stage Adjustment: {}%, Final Claim: ₱{}",
                    cropType, varietyPlanted, areaInsured, aiResult.getSeverity(), coveragePerHectare, totalAmountOfCover,
                    stageAdjustmentFactor * 100, claimAmount);

            return claimAmount;

        } catch (Exception e) {
            log.error("Error calculating AI claim amount", e);
            throw ApiException.badRequest("Failed to calculate AI claim amount: " + e.getMessage());
        }
    }

    private JsonNode createAIFieldValues(AIResultDTO aiResult) {
        try {
            var fieldValues = objectMapper.createObjectNode();
            fieldValues.put("ai_result_id", aiResult.getId());
            fieldValues.put("disease_type", aiResult.getResult());
            fieldValues.put("severity_percentage", aiResult.getSeverity());
            fieldValues.put("confidence", aiResult.getConfidence());
            fieldValues.put("accuracy", aiResult.getAccuracy());
            fieldValues.put("lesion_area", aiResult.getLesion_area());
            fieldValues.put("leaf_area", aiResult.getLeaf_area());
            fieldValues.put("prediction", aiResult.getPrediction());

            return fieldValues;
        } catch (Exception e) {
            log.error("Error creating AI field values", e);
            throw ApiException.internalError("Failed to create AI field values");
        }
    }

    private double getDoubleValue(JsonNode node, String fieldName) {
        JsonNode valueNode = node.get(fieldName);
        if (valueNode == null) {
            throw ApiException.badRequest("Required field '" + fieldName + "' not found in inspection data");
        }

        if (valueNode.isNumber()) {
            return valueNode.asDouble();
        } else if (valueNode.isTextual()) {
            try {
                return Double.parseDouble(valueNode.asText());
            } catch (NumberFormatException e) {
                throw ApiException.badRequest("Invalid number format for field '" + fieldName + "'");
            }
        } else {
            throw ApiException.badRequest("Field '" + fieldName + "' must be a number");
        }
    }

    private String getStringValue(JsonNode node, String fieldName, String defaultValue) {
        JsonNode valueNode = node.get(fieldName);
        if (valueNode == null || valueNode.isNull()) {
            return defaultValue;
        }
        return valueNode.asText(defaultValue);
    }

    private double getCoveragePerHectare(String cropType, String varietyPlanted) {
        // PCIC Coverage rates per hectare (Self-Financed rates)
        // Based on the rates you provided
        if ("Rice".equalsIgnoreCase(cropType)) {
            String variety = varietyPlanted.toLowerCase();
            if (variety.contains("inbred")) {
                if (variety.contains("seed production")) {
                    return 50000.0; // Inbred (Seed Production)
                } else {
                    return 41000.0; // Inbred (Commercial)
                }
            } else if (variety.contains("hybrid")) {
                if (variety.contains("seed production")) {
                    return 120000.0; // Hybrid (Seed Production A×R)
                } else {
                    return 50000.0; // Hybrid (Commercial F1)
                }
            } else {
                return 41000.0; // Default to Inbred Commercial
            }
        }

        // Default coverage for other crops (you can expand this based on your needs)
        return 41000.0;
    }

    private double getStageAdjustmentFactor(JsonNode inspectionValues) {
        try {
            // Get cultivation stage from inspection data
            String cultivationStage = getStringValue(inspectionValues, "cultivation_stage", "");

            if (cultivationStage.isEmpty()) {
                log.warn("Cultivation stage not found, using default adjustment factor");
                return 0.80; // Default adjustment factor
            }

            // Parse cultivation stage and determine adjustment factor
            // Based on PCIC guidelines - earlier stages get lower payouts
            String stage = cultivationStage.toLowerCase();

            if (stage.contains("1 month") || stage.contains("early") || stage.contains("seedling")) {
                return 0.60; // Early stage - 60% adjustment
            } else if (stage.contains("2 month") || stage.contains("vegetative")) {
                return 0.70; // Vegetative stage - 70% adjustment
            } else if (stage.contains("3 month") || stage.contains("reproductive") || stage.contains("flowering")) {
                return 0.85; // Reproductive stage - 85% adjustment
            } else if (stage.contains("4 month") || stage.contains("5 month") || stage.contains("maturity") || stage.contains("harvest")) {
                return 1.0; // Maturity/Harvest stage - 100% adjustment
            }

            // Try to extract number of months for more precise calculation
            if (stage.matches(".*\\d+.*month.*")) {
                try {
                    String monthStr = stage.replaceAll("[^\\d]", "");
                    if (!monthStr.isEmpty()) {
                        int months = Integer.parseInt(monthStr);
                        if (months <= 1) return 0.60;
                        else if (months == 2) return 0.70;
                        else if (months == 3) return 0.85;
                        else return 1.0; // months >= 4
                    }
                } catch (NumberFormatException e) {
                    log.warn("Could not parse month from cultivation stage: {}", cultivationStage);
                }
            }

            // Default adjustment factor if stage cannot be determined
            return 0.80;

        } catch (Exception e) {
            log.error("Error determining stage adjustment factor", e);
            return 0.80; // Safe default
        }
    }

    private String getCocNumberFromInspection(JsonNode inspectionValues) {
        log.debug("Checking for COC/Policy number in inspection field values: {}", inspectionValues);

        // List of possible field names for COC/Policy number
        String[] possibleFields = {
            "coc_no", "coc_number", "policy_number", "pol_no",
            "policy_no", "cic_no", "cic_number", "certificate_number"
        };

        for (String fieldName : possibleFields) {
            JsonNode valueNode = inspectionValues.get(fieldName);
            if (valueNode != null && !valueNode.isNull()) {
                String value = valueNode.asText();
                if (value != null && !value.trim().isEmpty()) {
                    log.info("Found COC/Policy number in field '{}': {}", fieldName, value);
                    return value.trim();
                }
            }
        }

        // Log all available field names for debugging
        List<String> availableFields = new ArrayList<>();
        inspectionValues.fieldNames().forEachRemaining(availableFields::add);
        log.warn("No COC/Policy number found in inspection field values. Available fields: {}", availableFields);
        return null;
    }

}
