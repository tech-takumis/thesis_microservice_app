package com.hashjosh.insurance.service;

import com.hashjosh.insurance.config.CustomUserDetails;
import com.hashjosh.insurance.dto.verification.VerificationRequest;
import com.hashjosh.insurance.dto.verification.VerificationResponse;
import com.hashjosh.insurance.entity.Insurance;
import com.hashjosh.insurance.entity.Verification;
import com.hashjosh.insurance.exception.ApiException;
import com.hashjosh.insurance.mapper.VerificationMapper;
import com.hashjosh.insurance.repository.InsuranceRepository;
import com.hashjosh.insurance.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final InsuranceRepository insuranceRepository;
    private final VerificationMapper verificationMapper;

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
