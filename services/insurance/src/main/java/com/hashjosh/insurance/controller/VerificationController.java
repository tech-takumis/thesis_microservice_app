package com.hashjosh.insurance.controller;

import com.hashjosh.insurance.dto.verification.VerificationRequest;
import com.hashjosh.insurance.dto.verification.VerificationResponse;
import com.hashjosh.insurance.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Spliterators;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/v1/verifications")
@RequiredArgsConstructor
@Slf4j
public class VerificationController {

    private final VerificationService verificationService;


    @PostMapping("/submission/{submissionId}")
    public ResponseEntity<VerificationResponse> createVerification(
            @PathVariable UUID submissionId,
            @Valid @RequestBody VerificationRequest request) {

        VerificationResponse response = verificationService.createVerificationBySubmissionId(submissionId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(value = "/submission/{submissionId}/with-files", consumes = {"multipart/form-data"})
    public ResponseEntity<VerificationResponse> createVerificationWithFiles(
            @PathVariable UUID submissionId,
            @Valid @RequestPart(value = "verification") VerificationRequest request,
            MultipartHttpServletRequest httpRequest
    ) {

        // Handle file uploads similar to ApplicationController
        Map<String, MultipartFile> fileMap = new HashMap<>();
        var fileNames = httpRequest.getFileNames();

        fileNames = httpRequest.getFileNames(); // Reset iterator
        while (fileNames.hasNext()) {
            String paramName = fileNames.next();
            if (!"verification".equals(paramName)) {
                MultipartFile file = httpRequest.getFile(paramName);
                if (file != null && !file.isEmpty()) {
                    fileMap.put(paramName, file);
                    log.info("Received verification file: {} -> {} (size: {} bytes, type: {})",
                        paramName, file.getOriginalFilename(), file.getSize(), file.getContentType());
                } else {
                    log.warn("File part {} is null or empty", paramName);
                }
            }
        }

        log.info("Total files to process: {}", fileMap.size());
        VerificationResponse response = verificationService.createVerificationWithFilesBySubmissionId(submissionId, request, fileMap);
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

    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<VerificationResponse> getVerificationBySubmissionId(@PathVariable UUID submissionId) {
        VerificationResponse response = verificationService.getVerificationBySubmissionId(submissionId);
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

    @PutMapping(value = "/{verificationId}/with-files", consumes = {"multipart/form-data"})
    public ResponseEntity<VerificationResponse> updateVerificationWithFiles(
            @PathVariable UUID verificationId,
            @Valid @RequestPart(value = "verification") VerificationRequest request,
            MultipartHttpServletRequest httpRequest
    ) {
        // Handle file uploads similar to ApplicationController
        Map<String, MultipartFile> fileMap = new HashMap<>();

        var fileNames = httpRequest.getFileNames();
        while (fileNames.hasNext()) {
            String paramName = fileNames.next();
            if (!"verification".equals(paramName)) {
                MultipartFile file = httpRequest.getFile(paramName);
                if (file != null && !file.isEmpty()) {
                    fileMap.put(paramName, file);
                    log.info("Received verification file for update: {} -> {}", paramName, file.getOriginalFilename());
                }
            }
        }

        VerificationResponse response = verificationService.updateVerificationWithFiles(verificationId, request, fileMap);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{verificationId}")
    public ResponseEntity<Void> deleteVerification(@PathVariable UUID verificationId) {
        verificationService.deleteVerification(verificationId);
        return ResponseEntity.noContent().build();
    }
}
