package com.hashjosh.application.controller;

import com.hashjosh.constant.ai.AIResultDTO;
import com.hashjosh.constant.application.ApplicationResponseDto;
import com.hashjosh.application.dto.submission.ApplicationSubmissionDto;
import com.hashjosh.application.dto.submission.ApplicationSubmissionResponse;
import com.hashjosh.application.model.Application;
import com.hashjosh.application.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping(value = "/submit", consumes = {"multipart/form-data"})
    public ResponseEntity<ApplicationSubmissionResponse> submitApplication(
            @Valid @RequestPart(value = "submission") ApplicationSubmissionDto submission,
            MultipartHttpServletRequest request
    ) {
        Map<String, MultipartFile> fileMap = new HashMap<>();

        // Get all file parts except submission
        var fileNames = request.getFileNames();
        while (fileNames.hasNext()) {
            String paramName = fileNames.next();
            if (!"submission".equals(paramName)) {
                MultipartFile file = request.getFile(paramName);
                if (file != null && !file.isEmpty()) {
                    fileMap.put(paramName, file);
                    System.out.println("Received file: " + paramName + " -> " + file.getOriginalFilename());
                }
            }
        }

        Application application = applicationService.processSubmission(submission, fileMap);
        return ResponseEntity.ok(ApplicationSubmissionResponse.builder()
                .applicationId(application.getId())
                .success(true)
                .message("Application submitted successfully")
                .build());
    }

    @GetMapping()
    public ResponseEntity<List<ApplicationResponseDto>> findAllApplications(
            @RequestParam(value = "provider", required = false) String provider,
            @RequestParam(value = "type", required = false) Boolean type,
            @RequestParam(value = "field-values", required = false) Boolean fieldValues,
            @RequestParam(value = "farmer", required = false) Boolean farmer,
            @RequestParam(value = "files", required = false) Boolean files
    ){
        if (provider != null && !provider.isEmpty()) {
            return new ResponseEntity<>(applicationService.findAllApplication(provider, type, farmer, fieldValues, files), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(applicationService.findAll(fieldValues,type,farmer,files), HttpStatus.OK);
        }
    }

    @GetMapping("/{application-id}")
    public ResponseEntity<ApplicationResponseDto> findById(
            @PathVariable("application-id") UUID applicationId,
            @RequestParam(value = "type", required = false) Boolean type,
            @RequestParam(value = "field-values", required = false) Boolean fieldValues,
            @RequestParam(value = "farmer", required = false) Boolean farmer,
            @RequestParam(value = "files", required = false) Boolean files
    ){
        return  ResponseEntity.ok(applicationService.getApplicationById(applicationId, type, farmer, fieldValues,files));
    }

    @PutMapping("/{application-id}/update-documents")
    public ResponseEntity<String> updateApplicationDocuments(
            @PathVariable("application-id") UUID applicationId,
            @RequestParam("files") List<MultipartFile> files
    ){
        applicationService.updateApplicationDocuments(applicationId,files);
        return new ResponseEntity<>("Application documents updated successfully",HttpStatus.OK);
    }

    @DeleteMapping("/{application-id}")
    public ResponseEntity<Void> deleteApplication(
            @PathVariable("application-id") UUID applicationId
    ) {
        applicationService.deleteApplication(applicationId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/ai/{application_id}/test")
    public ResponseEntity<AIResultDTO> getApplicationAIResult(
            @PathVariable("application_id") UUID applicationId
    ){
        return ResponseEntity.ok(applicationService.getApplicationAIResult(applicationId));
    }
}
