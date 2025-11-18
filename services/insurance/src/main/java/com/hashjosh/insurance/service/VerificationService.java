package com.hashjosh.insurance.service;

import com.hashjosh.insurance.clients.DocumentServiceClient;
import com.hashjosh.insurance.config.CustomUserDetails;
import com.hashjosh.insurance.dto.verification.VerificationRequest;
import com.hashjosh.insurance.dto.verification.VerificationResponse;
import com.hashjosh.insurance.entity.Insurance;
import com.hashjosh.insurance.entity.Verification;
import com.hashjosh.insurance.exception.ApiException;
import com.hashjosh.insurance.mapper.VerificationMapper;
import com.hashjosh.insurance.repository.InsuranceRepository;
import com.hashjosh.insurance.repository.VerificationRepository;
import com.hashjosh.constant.document.dto.DocumentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final InsuranceRepository insuranceRepository;
    private final VerificationMapper verificationMapper;
    private final DocumentServiceClient documentServiceClient;

    public VerificationResponse createVerification(UUID insuranceId, VerificationRequest request) {
        // Get verifier info from SecurityContext
        CustomUserDetails userDetails = getCurrentUserDetails();

        // Find the insurance
        Insurance insurance = insuranceRepository.findById(insuranceId)
                .orElseThrow(() -> new ApiException("Insurance not found", HttpStatus.NOT_FOUND));

        // Check if verification already exists for this insurance
        if (verificationRepository.findByInsuranceId(insuranceId).isPresent()) {
            throw new ApiException("Verification already exists for this insurance", HttpStatus.CONFLICT);
        }

        // Create verification
        Verification verification = verificationMapper.toEntity(
                request,
                insurance,
                userDetails.getUserId(),
                userDetails.getFirstName() + " " + userDetails.getLastName()
        );

        Verification savedVerification = verificationRepository.save(verification);
        return verificationMapper.toResponse(savedVerification);
    }

    public VerificationResponse createVerificationBySubmissionId(UUID submissionId, VerificationRequest request) {
        // Get verifier info from SecurityContext
        CustomUserDetails userDetails = getCurrentUserDetails();

        // Find the insurance by submissionId
        Insurance insurance = insuranceRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new ApiException("Insurance not found for submission ID: " + submissionId, HttpStatus.NOT_FOUND));

        // Check if verification already exists for this insurance
        if (verificationRepository.findByInsuranceId(insurance.getId()).isPresent()) {
            throw new ApiException("Verification already exists for this insurance", HttpStatus.CONFLICT);
        }

        // Create verification
        Verification verification = verificationMapper.toEntity(
                request,
                insurance,
                userDetails.getUserId(),
                userDetails.getFirstName() + " " + userDetails.getLastName()
        );

        Verification savedVerification = verificationRepository.save(verification);
        return verificationMapper.toResponse(savedVerification);
    }

    public VerificationResponse createVerificationWithFiles(
            UUID insuranceId,
            VerificationRequest request,
            Map<String, MultipartFile> fileMap
    ) {
        try {
            // Get verifier info from SecurityContext
            CustomUserDetails userDetails = getCurrentUserDetails();

            // Find the insurance
            Insurance insurance = insuranceRepository.findById(insuranceId)
                    .orElseThrow(() -> new ApiException("Insurance not found", HttpStatus.NOT_FOUND));

            // Check if verification already exists for this insurance
            if (verificationRepository.findByInsuranceId(insuranceId).isPresent()) {
                throw new ApiException("Verification already exists for this insurance", HttpStatus.CONFLICT);
            }

            // Upload files and get document IDs
            List<UUID> documentIds = new ArrayList<>();
            if (fileMap != null && !fileMap.isEmpty()) {
                for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
                    MultipartFile file = entry.getValue();

                    // Upload document using DocumentServiceClient
                    DocumentResponse docResponse = documentServiceClient.uploadDocument(file, userDetails.getUserId().toString());
                    documentIds.add(docResponse.getDocumentId());

                    log.info("Uploaded verification document: {} -> {}", entry.getKey(), file.getOriginalFilename());
                }
            }

            // Create verification with document IDs
            Verification verification = verificationMapper.toEntity(
                    request,
                    insurance,
                    userDetails.getUserId(),
                    userDetails.getFirstName() + " " + userDetails.getLastName(),
                    documentIds
            );

            Verification savedVerification = verificationRepository.save(verification);
            return verificationMapper.toResponse(savedVerification);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create verification with files for insurance {}: {}", insuranceId, e.getMessage(), e);
            throw new ApiException("Failed to create verification: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public VerificationResponse createVerificationWithFilesBySubmissionId(
            UUID submissionId,
            VerificationRequest request,
            Map<String, MultipartFile> fileMap
    ) {
        try {
            // Get verifier info from SecurityContext
            CustomUserDetails userDetails = getCurrentUserDetails();

            // Find the insurance by submissionId
            Insurance insurance = insuranceRepository.findBySubmissionId(submissionId)
                    .orElseThrow(() -> new ApiException("Insurance not found for submission ID: " + submissionId, HttpStatus.NOT_FOUND));

            // Check if verification already exists for this insurance
            if (verificationRepository.findByInsuranceId(insurance.getId()).isPresent()) {
                throw new ApiException("Verification already exists for this insurance", HttpStatus.CONFLICT);
            }

            // Upload files and get document IDs
            List<UUID> documentIds = new ArrayList<>();
            if (fileMap != null && !fileMap.isEmpty()) {

                for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
                    MultipartFile file = entry.getValue();

                    log.info("Processing file: key={}, filename={}, size={}, contentType={}",
                        entry.getKey(), file.getOriginalFilename(), file.getSize(), file.getContentType());

                    try {
                        // Upload document using DocumentServiceClient
                        DocumentResponse docResponse = documentServiceClient.uploadDocument(file, userDetails.getUserId().toString());
                        documentIds.add(docResponse.getDocumentId());

                        log.info("Successfully uploaded verification document: {} -> {} with ID: {}",
                            entry.getKey(), file.getOriginalFilename(), docResponse.getDocumentId());
                    } catch (Exception e) {
                        log.error("Failed to upload file {} ({}): {}", entry.getKey(), file.getOriginalFilename(), e.getMessage());
                        throw new ApiException("Failed to upload file: " + file.getOriginalFilename(), HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
            } else {
                log.info("No files to upload for verification");
            }

            // Create verification with document IDs
            Verification verification = verificationMapper.toEntity(
                    request,
                    insurance,
                    userDetails.getUserId(),
                    userDetails.getFirstName() + " " + userDetails.getLastName(),
                    documentIds
            );

            Verification savedVerification = verificationRepository.save(verification);
            return verificationMapper.toResponse(savedVerification);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create verification with files for submission {}: {}", submissionId, e.getMessage(), e);
            throw new ApiException("Failed to create verification: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public VerificationResponse getVerificationById(UUID verificationId) {
        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ApiException("Verification not found", HttpStatus.NOT_FOUND));

        return verificationMapper.toResponse(verification);
    }

    @Transactional(readOnly = true)
    public VerificationResponse getVerificationByInsuranceId(UUID insuranceId) {
        Verification verification = verificationRepository.findByInsuranceId(insuranceId)
                .orElseThrow(() -> new ApiException("Verification not found for insurance", HttpStatus.NOT_FOUND));

        return verificationMapper.toResponse(verification);
    }

    public VerificationResponse getVerificationBySubmissionId(UUID submissionId) {
        Verification verification = verificationRepository.findByInsuranceId(submissionId)
                .orElseThrow(() -> new ApiException("Verification not found for submission", HttpStatus.NOT_FOUND));
        return verificationMapper.toResponse(verification);
    }

    @Transactional(readOnly = true)
    public Page<VerificationResponse> getAllVerifications(Pageable pageable) {
        Page<Verification> verifications = verificationRepository.findAll(pageable);
        return verifications.map(verificationMapper::toResponse);
    }

    public VerificationResponse updateVerification(UUID verificationId, VerificationRequest request) {
        // Get verifier info from SecurityContext
        CustomUserDetails userDetails = getCurrentUserDetails();

        // Find existing verification
        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ApiException("Verification not found", HttpStatus.NOT_FOUND));

        // Update verification
        verificationMapper.updateEntity(
                verification,
                request,
                userDetails.getUserId(),
                userDetails.getFirstName() + " " + userDetails.getLastName()
        );

        Verification updatedVerification = verificationRepository.save(verification);
        return verificationMapper.toResponse(updatedVerification);
    }

    public VerificationResponse updateVerificationWithFiles(
            UUID verificationId,
            VerificationRequest request,
            Map<String, MultipartFile> fileMap
    ) {
        try {
            // Get verifier info from SecurityContext
            CustomUserDetails userDetails = getCurrentUserDetails();

            // Find existing verification
            Verification verification = verificationRepository.findById(verificationId)
                    .orElseThrow(() -> new ApiException("Verification not found", HttpStatus.NOT_FOUND));

            // Upload new files and get document IDs
            List<UUID> documentIds = new ArrayList<>();
            if (fileMap != null && !fileMap.isEmpty()) {
                for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
                    MultipartFile file = entry.getValue();

                    // Upload document using DocumentServiceClient
                    DocumentResponse docResponse = documentServiceClient.uploadDocument(file, userDetails.getUserId().toString());
                    documentIds.add(docResponse.getDocumentId());

                    log.info("Uploaded verification document for update: {} -> {}", entry.getKey(), file.getOriginalFilename());
                }
            }

            // Update verification with new document IDs (merge with existing if needed)
            if (!documentIds.isEmpty()) {
                // If request has existing documents, merge them
                if (request.getVerificationDocuments() != null) {
                    List<UUID> allDocuments = new ArrayList<>(request.getVerificationDocuments());
                    allDocuments.addAll(documentIds);
                    request.setVerificationDocuments(allDocuments);
                } else {
                    request.setVerificationDocuments(documentIds);
                }
            }

            // Update verification entity
            verificationMapper.updateEntity(
                    verification,
                    request,
                    userDetails.getUserId(),
                    userDetails.getFirstName() + " " + userDetails.getLastName()
            );

            Verification updatedVerification = verificationRepository.save(verification);
            return verificationMapper.toResponse(updatedVerification);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update verification with files {}: {}", verificationId, e.getMessage(), e);
            throw new ApiException("Failed to update verification: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteVerification(UUID verificationId) {
        if (!verificationRepository.existsById(verificationId)) {
            throw new ApiException("Verification not found", HttpStatus.NOT_FOUND);
        }

        verificationRepository.deleteById(verificationId);
    }

    private CustomUserDetails getCurrentUserDetails() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            throw new ApiException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        return (CustomUserDetails) principal;
    }
}
