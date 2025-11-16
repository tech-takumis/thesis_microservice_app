package com.hashjosh.insurance.controller;

import com.hashjosh.insurance.dto.inspection.InspectionRequest;
import com.hashjosh.insurance.dto.inspection.InspectionResponse;
import com.hashjosh.insurance.service.InspectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inspections")
@RequiredArgsConstructor
@Slf4j
public class InspectionController {

    private final InspectionService inspectionService;

    @PostMapping("/insurance/{insuranceId}")
    public ResponseEntity<InspectionResponse> createInspection(
            @PathVariable UUID insuranceId,
            @Valid @RequestBody InspectionRequest request) {
        log.info("Creating inspection for insurance ID: {}", insuranceId);
        InspectionResponse response = inspectionService.createInspection(insuranceId, request);
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

    @GetMapping("/inspector/{inspectorId}")
    public ResponseEntity<List<InspectionResponse>> getInspectionsByInspectorId(@PathVariable UUID inspectorId) {
        log.info("Getting inspections by inspector ID: {}", inspectorId);
        List<InspectionResponse> responses = inspectionService.getInspectionsByInspectorId(inspectorId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<InspectionResponse> getInspectionByScheduleId(@PathVariable UUID scheduleId) {
        log.info("Getting inspection by schedule ID: {}", scheduleId);
        InspectionResponse response = inspectionService.getInspectionByScheduleId(scheduleId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<InspectionResponse>> getAllInspections() {
        log.info("Getting all inspections");
        List<InspectionResponse> responses = inspectionService.getAllInspections();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{inspectionId}")
    public ResponseEntity<InspectionResponse> updateInspection(
            @PathVariable UUID inspectionId,
            @Valid @RequestBody InspectionRequest request) {
        log.info("Updating inspection with ID: {}", inspectionId);
        InspectionResponse response = inspectionService.updateInspection(inspectionId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{inspectionId}")
    public ResponseEntity<Void> deleteInspection(@PathVariable UUID inspectionId) {
        log.info("Deleting inspection with ID: {}", inspectionId);
        inspectionService.deleteInspection(inspectionId);
        return ResponseEntity.noContent().build();
    }
}
