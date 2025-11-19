package com.hashjosh.insurance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashjosh.insurance.config.CustomUserDetails;
import com.hashjosh.insurance.dto.claim.ClaimAIRequest;
import com.hashjosh.insurance.dto.claim.ClaimRequest;
import com.hashjosh.insurance.dto.claim.ClaimResponse;
import com.hashjosh.insurance.service.ClaimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
@Slf4j
public class ClaimController {

    private final ClaimService claimService;
    private final ObjectMapper objectMapper;

    private String getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUserId() != null ? userDetails.getUserId().toString() : null;
        }
        log.warn("No authenticated user found in security context");
        return null;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClaimResponse> createClaimManually(
            @RequestPart("claim") String claimJson,
            @RequestPart(value = "supportingFiles", required = false) List<MultipartFile> supportingFiles) {

        try {
            String userId = getAuthenticatedUserId();
            ClaimRequest request = objectMapper.readValue(claimJson, ClaimRequest.class);
            log.info("Creating manual claim for insurance ID: {} by user: {}", request.getInsuranceId(), userId);

            ClaimResponse response = claimService.createClaimManually(request, supportingFiles, userId);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            log.error("Error creating manual claim", e);
            throw new RuntimeException("Failed to parse claim request: " + e.getMessage());
        }
    }

    @PostMapping(value = "/ai", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClaimResponse> createClaimFromAI(
            @RequestPart("claim") String claimJson,
            @RequestPart(value = "supportingFiles", required = false) List<MultipartFile> supportingFiles) {

        try {
            String userId = getAuthenticatedUserId();
            ClaimAIRequest request = objectMapper.readValue(claimJson, ClaimAIRequest.class);
            log.info("Creating AI claim for insurance ID: {} with application ID: {} by user: {}",
                    request.getInsuranceId(), request.getApplicationId(), userId);

            ClaimResponse response = claimService.createClaimFromAI(request, supportingFiles, userId);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            log.error("Error creating AI claim", e);
            throw new RuntimeException("Failed to parse AI claim request: " + e.getMessage());
        }
    }

    @GetMapping("/{claimId}")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable UUID claimId) {
        log.info("Getting claim by ID: {}", claimId);
        ClaimResponse response = claimService.getClaimById(claimId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ClaimResponse>> getAllClaims() {
        log.info("Getting all claims");
        List<ClaimResponse> response = claimService.getAllClaims();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/insurance/{insuranceId}")
    public ResponseEntity<List<ClaimResponse>> getClaimsByInsuranceId(
            @PathVariable UUID insuranceId) {
        log.info("Getting claims by insurance ID: {}", insuranceId);
        List<ClaimResponse> response = claimService.getClaimsByInsuranceId(insuranceId);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{claimId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClaimResponse> updateClaim(
            @PathVariable UUID claimId,
            @RequestPart("claim") String claimJson,
            @RequestPart(value = "supportingFiles", required = false) List<MultipartFile> supportingFiles) {

        try {
            String userId = getAuthenticatedUserId();
            ClaimRequest request = objectMapper.readValue(claimJson, ClaimRequest.class);
            log.info("Updating claim ID: {} by user: {}", claimId, userId);

            ClaimResponse response = claimService.updateClaim(claimId, request, supportingFiles, userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating claim", e);
            throw new RuntimeException("Failed to parse claim update request: " + e.getMessage());
        }
    }

    @DeleteMapping("/{claimId}")
    public ResponseEntity<Void> deleteClaim(@PathVariable UUID claimId) {
        log.info("Deleting claim ID: {}", claimId);
        claimService.deleteClaim(claimId);
        return ResponseEntity.noContent().build();
    }
}
