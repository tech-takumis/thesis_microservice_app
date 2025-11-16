package com.hashjosh.insurance.controller;

import com.hashjosh.insurance.dto.verification.VerificationRequest;
import com.hashjosh.insurance.dto.verification.VerificationResponse;
import com.hashjosh.insurance.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/verifications")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;


    @PostMapping("/insurance/{insuranceId}")
    public ResponseEntity<VerificationResponse> createVerification(
            @PathVariable UUID insuranceId,
            @Valid @RequestBody VerificationRequest request) {

        VerificationResponse response = verificationService.createVerification(insuranceId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @GetMapping("/{verificationId}")
    public ResponseEntity<VerificationResponse> getVerificationById(@PathVariable UUID verificationId) {
        VerificationResponse response = verificationService.getVerificationById(verificationId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/insurance/{insuranceId}")
    public ResponseEntity<VerificationResponse> getVerificationByInsuranceId(@PathVariable UUID insuranceId) {
        VerificationResponse response = verificationService.getVerificationByInsuranceId(insuranceId);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<Page<VerificationResponse>> getAllVerifications(Pageable pageable) {
        Page<VerificationResponse> response = verificationService.getAllVerifications(pageable);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{verificationId}")
    public ResponseEntity<VerificationResponse> updateVerification(
            @PathVariable UUID verificationId,
            @Valid @RequestBody VerificationRequest request) {

        VerificationResponse response = verificationService.updateVerification(verificationId, request);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{verificationId}")
    public ResponseEntity<Void> deleteVerification(@PathVariable UUID verificationId) {
        verificationService.deleteVerification(verificationId);
        return ResponseEntity.noContent().build();
    }
}
