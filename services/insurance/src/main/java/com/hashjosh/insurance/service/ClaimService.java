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
                .supportingFiles(documentIds)
                .fieldValues(request.getFieldValues())
                .build();

        claim = claimRepository.save(claim);
        log.info("Claim created successfully with ID: {}", claim.getId());
        insurance.setCurrentStatus(InsuranceStatus.CLAIMED_ISSUED);
        insuranceRepository.save(insurance);

        ClaimProcessedEvent event = ClaimProcessedEvent.builder()
                .submissionId(insurance.getSubmissionId())
                .userId(insurance.getFarmerId())
                .claimId(claim.getId())
                .claimAmount(claimAmount)
                .processedAt(LocalDateTime.now())
                .build();

        kafkaProducer.publishEvent("application-claim",event);

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
                .supportingFiles(documentIds)
                .fieldValues(aiFieldValues)
                .build();

        claim = claimRepository.save(claim);
        log.info("Claim created from AI successfully with ID: {}", claim.getId());

        insurance.setCurrentStatus(InsuranceStatus.CLAIMED_ISSUED);
        insuranceRepository.save(insurance);

        ClaimProcessedEvent event = ClaimProcessedEvent.builder()
                .submissionId(insurance.getSubmissionId())
                .userId(insurance.getFarmerId())
                .claimId(claim.getId())
                .claimAmount(claimAmount)
                .processedAt(LocalDateTime.now())
                .build();

        kafkaProducer.publishEvent("application-claim",event);

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

    public List<ClaimResponse> getClaimsByInsuranceId(UUID insuranceId) {
        List<Claim> claims = claimRepository.findByInsuranceId(insuranceId)
                .map(List::of)
                .orElse(List.of());
        return claims.stream()
                .map(claimMapper::toResponse)
                .toList();
    }

    @Transactional
    public ClaimResponse updateClaim(UUID claimId, ClaimRequest request, List<MultipartFile> supportingFiles, String userId) {
        Claim existingClaim = claimRepository.findById(claimId)
                .orElseThrow(() -> ApiException.notFound("Claim not found with ID: " + claimId));

        // Upload new supporting files if provided
        if (supportingFiles != null && !supportingFiles.isEmpty()) {
            List<UUID> newDocumentIds = uploadSupportingFiles(supportingFiles, userId);
            List<UUID> allDocumentIds = new ArrayList<>(existingClaim.getSupportingFiles());
            allDocumentIds.addAll(newDocumentIds);
            existingClaim.setSupportingFiles(allDocumentIds);
        }

        // Update fields
        if (request.getDamageAssessment() != null) {
            existingClaim.setDamageAssessment(request.getDamageAssessment());
        }

        if (request.getFieldValues() != null) {
            existingClaim.setFieldValues(request.getFieldValues());
            // Recalculate claim amount if field values changed
            Double newClaimAmount = calculateClaimAmount(existingClaim.getInsurance(), request.getFieldValues());
            existingClaim.setClaimAmount(newClaimAmount);
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

        if (insurance.getPolicy() == null) {
            throw ApiException.badRequest("Insurance must have a policy before creating a claim");
        }
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
                        if (months == 2) return 0.70;
                        if (months == 3) return 0.85;
                        if (months >= 4) return 1.0;
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


}
