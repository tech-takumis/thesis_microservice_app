package com.hashjosh.application.controller;

import com.hashjosh.application.dto.submission.ApplicationSubmissionDto;
import com.hashjosh.application.dto.submission.ApplicationSubmissionResponse;
import com.hashjosh.application.dto.update.ApplicationUpdateDto;
import com.hashjosh.application.dto.update.ApplicationUpdateResponse;
import com.hashjosh.application.dto.workflow.ApplicationWorkflowResponse;
import com.hashjosh.application.model.Application;
import com.hashjosh.application.model.ApplicationWorkflow;
import com.hashjosh.application.service.ApplicationService;
import com.hashjosh.constant.application.ApplicationResponseDto;
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
            @RequestParam(value = "provider", required = false) String provider
    ){
        if (provider != null && !provider.isEmpty()) {
            return new ResponseEntity<>(applicationService.findAllApplication(provider), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(applicationService.findAll(), HttpStatus.OK);
        }
    }

    @GetMapping("/user/all")
    public ResponseEntity<List<ApplicationResponseDto>> findApplicationsByCurrentUser(){
        return ResponseEntity.ok(applicationService.findByCurrentUser());
    }

    @GetMapping("/{applicationId}/required-ai-analysis")
    public ResponseEntity<Boolean> isAiAnalysisRequired(
            @PathVariable("applicationId") UUID applicationId
    ){
        boolean isRequired = applicationService.isAiAnalysisRequired(applicationId);
        return ResponseEntity.ok(isRequired);
    }

    @GetMapping("/{application-id}/workflow")
    public ResponseEntity<ApplicationWorkflowResponse> getApplicationWorkflow(
            @PathVariable("application-id") UUID applicationId
    ){
        return ResponseEntity.ok(applicationService.getApplicationWorkflow(applicationId));
    }

    @GetMapping("/{application-id}")
    public ResponseEntity<ApplicationResponseDto> findById(
            @PathVariable("application-id") UUID applicationId
    ){
        return  ResponseEntity.ok(applicationService.getApplicationById(applicationId));
    }

    @PutMapping("/{application-id}/update-documents")
    public ResponseEntity<String> updateApplicationDocuments(
            @PathVariable("application-id") UUID applicationId,
            @RequestParam("files") List<MultipartFile> files
    ){
        applicationService.updateApplicationDocuments(applicationId,files);
        return new ResponseEntity<>("Application documents updated successfully",HttpStatus.OK);
    }

    @PutMapping(value = "/{application-id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApplicationUpdateResponse> updateApplication(
            @PathVariable("application-id") UUID applicationId,
            @Valid @RequestPart(value = "update", required = false) ApplicationUpdateDto updateDto,
            MultipartHttpServletRequest request
    ) {
        // Handle file uploads
        Map<String, MultipartFile> fileMap = new HashMap<>();

        var fileNames = request.getFileNames();
        while (fileNames.hasNext()) {
            String paramName = fileNames.next();
            if (!"update".equals(paramName)) {
                MultipartFile file = request.getFile(paramName);
                if (file != null && !file.isEmpty()) {
                    fileMap.put(paramName, file);
                    log.info("Received file for update: {} -> {}", paramName, file.getOriginalFilename());
                }
            }
        }

        // If no updateDto is provided, create an empty one
        if (updateDto == null) {
            updateDto = new ApplicationUpdateDto();
        }

        Application updatedApplication = applicationService.updateApplication(applicationId, updateDto, fileMap);

        return ResponseEntity.ok(ApplicationUpdateResponse.builder()
                .applicationId(updatedApplication.getId())
                .success(true)
                .message("Application updated successfully")
                .version(updatedApplication.getVersion())
                .build());
    }

    @PatchMapping(value = "/{application-id}/field", consumes = {"application/json"})
    public ResponseEntity<ApplicationUpdateResponse> updateSingleField(
            @PathVariable("application-id") UUID applicationId,
            @RequestParam("fieldName") String fieldName,
            @RequestBody Object fieldValue
    ) {
        ApplicationUpdateDto updateDto = new ApplicationUpdateDto();

        // Handle basic fields or dynamic fields
        switch (fieldName) {
            case "fullName" -> updateDto.setFullName(fieldValue.toString());
            case "coordinates" -> updateDto.setCoordinates(fieldValue.toString());
            default -> updateDto.setDynamicField(fieldName, fieldValue);
        }

        Application updatedApplication = applicationService.updateApplication(applicationId, updateDto, null);

        return ResponseEntity.ok(ApplicationUpdateResponse.builder()
                .applicationId(updatedApplication.getId())
                .success(true)
                .message("Field '" + fieldName + "' updated successfully")
                .version(updatedApplication.getVersion())
                .build());
    }

    @DeleteMapping("/{application-id}")
    public ResponseEntity<Void> deleteApplication(
            @PathVariable("application-id") UUID applicationId
    ) {
        applicationService.deleteApplication(applicationId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
