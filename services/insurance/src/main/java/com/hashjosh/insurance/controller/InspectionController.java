package com.hashjosh.insurance.controller;

import com.hashjosh.constant.program.dto.ScheduleRequestDto;
import com.hashjosh.constant.program.dto.ScheduleResponseDto;
import com.hashjosh.insurance.dto.inspection.InspectionRequest;
import com.hashjosh.insurance.dto.inspection.InspectionResponse;
import com.hashjosh.insurance.service.InspectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inspection")
@RequiredArgsConstructor
@Slf4j
public class InspectionController {

    private final InspectionService inspectionService;

    @PostMapping(value = "/insurance/{insuranceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InspectionResponse> createInspection(
            @PathVariable UUID insuranceId,
            @RequestPart("request") @Valid InspectionRequest request,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {
        log.info("Creating inspection for insurance ID: {} with {} photos", insuranceId, photos != null ? photos.size() : 0);
        InspectionResponse response = inspectionService.createInspection(insuranceId, request, photos);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{insuranceId}/schedule")
    public ResponseEntity<ScheduleResponseDto> createInspectionSchedule(
            @PathVariable UUID insuranceId,
            @Valid @RequestBody ScheduleRequestDto request) {
        log.info("Creating inspection schedule for insurance ID: {}", insuranceId);
        ScheduleResponseDto response = inspectionService.createInspectionSchedule(insuranceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{inspectionId}")
    public ResponseEntity<InspectionResponse> getInspectionById(@PathVariable UUID inspectionId) {
        log.info("Getting inspection by ID: {}", inspectionId);
        InspectionResponse response = inspectionService.getInspectionById(inspectionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/insurance/{insuranceId}")
    public ResponseEntity<InspectionResponse> getInspectionByInsuranceId(@PathVariable UUID insuranceId) {
        log.info("Getting inspection by insurance ID: {}", insuranceId);
        InspectionResponse response = inspectionService.getInspectionByInsuranceId(insuranceId);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<List<InspectionResponse>> getAllInspections() {
        log.info("Getting all inspections");
        List<InspectionResponse> responses = inspectionService.getAllInspections();
        return ResponseEntity.ok(responses);
    }

    @PutMapping(value = "/{inspectionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InspectionResponse> updateInspection(
            @PathVariable UUID inspectionId,
            @RequestPart("request") @Valid InspectionRequest request,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {
        log.info("Updating inspection with ID: {} with {} photos", inspectionId, photos != null ? photos.size() : 0);
        InspectionResponse response = inspectionService.updateInspection(inspectionId, request, photos);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{inspectionId}")
    public ResponseEntity<Void> deleteInspection(@PathVariable UUID inspectionId) {
        log.info("Deleting inspection with ID: {}", inspectionId);
        inspectionService.deleteInspection(inspectionId);
        return ResponseEntity.noContent().build();
    }
}
