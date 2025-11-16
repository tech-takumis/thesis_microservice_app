package com.hashjosh.insurance.service;

import com.hashjosh.constant.program.dto.ScheduleRequestDto;
import com.hashjosh.constant.program.dto.ScheduleResponseDto;
import com.hashjosh.insurance.clients.ScheduleClient;
import com.hashjosh.insurance.config.CustomUserDetails;
import com.hashjosh.insurance.dto.inspection.InspectionRequest;
import com.hashjosh.insurance.dto.inspection.InspectionResponse;
import com.hashjosh.insurance.entity.Inspection;
import com.hashjosh.insurance.entity.Insurance;
import com.hashjosh.insurance.exception.ApiException;
import com.hashjosh.insurance.mapper.InspectionMapper;
import com.hashjosh.insurance.repository.InspectionRepository;
import com.hashjosh.insurance.repository.InsuranceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final InspectionMapper inspectionMapper;
    private final InsuranceRepository insuranceRepository;
    private final ScheduleClient scheduleClient;

    @Transactional
    public InspectionResponse createInspection(UUID insuranceId, InspectionRequest request) {
        log.info("Creating inspection for insurance ID: {}", insuranceId);

        // Get current user from security context
        CustomUserDetails userDetails = getCurrentUserDetails();

        // Validate insurance exists
        Insurance insurance = insuranceRepository.findById(insuranceId)
                .orElseThrow(() -> ApiException.notFound("Insurance not found with ID: " + insuranceId));

        // Check if inspection already exists for this insurance
        if (inspectionRepository.existsByInsurance_Id(insuranceId)) {
            throw ApiException.conflict("Inspection already exists for insurance ID: " + insuranceId);
        }

        // Create schedule if scheduling data is provided
        ScheduleResponseDto schedule = null;
        if (request.getType() != null && request.getScheduleDate() != null) {
            ScheduleRequestDto scheduleRequest = ScheduleRequestDto.builder()
                    .type(request.getType())
                    .scheduleDate(request.getScheduleDate())
                    .priority(request.getPriority())
                    .metaData(request.getMetaData())
                    .build();

            schedule = scheduleClient.createSchedule(scheduleRequest, userDetails.getUserId().toString());
        }

        // Create inspection entity
        Inspection inspection = inspectionMapper.toEntity(request, insurance,
                userDetails.getUserId(), getInspectorName(userDetails));

        if (schedule != null) {
            inspection.setScheduleId(schedule.getId());
        }

        // Save inspection
        Inspection savedInspection = inspectionRepository.save(inspection);

        log.info("Inspection created successfully with ID: {}", savedInspection.getId());
        return inspectionMapper.toResponse(savedInspection, schedule);
    }

    @Transactional(readOnly = true)
    public InspectionResponse getInspectionById(UUID inspectionId) {
        log.info("Getting inspection by ID: {}", inspectionId);

        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> ApiException.notFound("Inspection not found with ID: " + inspectionId));

        ScheduleResponseDto schedule = null;
        if (inspection.getScheduleId() != null) {
            try {
                CustomUserDetails userDetails = getCurrentUserDetails();
                schedule = scheduleClient.getInspectionScheduleById(
                        inspection.getScheduleId().toString(),
                        userDetails.getUserId().toString()
                );
            } catch (Exception e) {
                log.warn("Failed to fetch schedule for inspection {}: {}", inspectionId, e.getMessage());
            }
        }

        return inspectionMapper.toResponse(inspection, schedule);
    }

    @Transactional(readOnly = true)
    public InspectionResponse getInspectionByInsuranceId(UUID insuranceId) {
        log.info("Getting inspection by insurance ID: {}", insuranceId);

        Inspection inspection = inspectionRepository.findByInsurance_Id(insuranceId)
                .orElseThrow(() -> ApiException.notFound("Inspection not found for insurance ID: " + insuranceId));

        return getInspectionById(inspection.getId());
    }

    @Transactional(readOnly = true)
    public List<InspectionResponse> getInspectionsByInspectorId(UUID inspectorId) {
        log.info("Getting inspections by inspector ID: {}", inspectorId);

        List<Inspection> inspections = inspectionRepository.findByInspectorId(inspectorId);
        return inspections.stream()
                .map(inspectionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InspectionResponse> getAllInspections() {
        log.info("Getting all inspections");

        List<Inspection> inspections = inspectionRepository.findAll();
        return inspections.stream()
                .map(inspectionMapper::toResponse)
                .toList();
    }

    @Transactional
    public InspectionResponse updateInspection(UUID inspectionId, InspectionRequest request) {
        log.info("Updating inspection with ID: {}", inspectionId);

        // Get current user from security context
        CustomUserDetails userDetails = getCurrentUserDetails();

        // Find existing inspection
        Inspection existingInspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> ApiException.notFound("Inspection not found with ID: " + inspectionId));

        // Update inspection entity
        inspectionMapper.updateEntity(existingInspection, request,
                userDetails.getUserId(), getInspectorName(userDetails));

        // Save updated inspection
        Inspection updatedInspection = inspectionRepository.save(existingInspection);

        log.info("Inspection updated successfully with ID: {}", updatedInspection.getId());
        return inspectionMapper.toResponse(updatedInspection);
    }

    @Transactional
    public void deleteInspection(UUID inspectionId) {
        log.info("Deleting inspection with ID: {}", inspectionId);

        if (!inspectionRepository.existsById(inspectionId)) {
            throw ApiException.notFound("Inspection not found with ID: " + inspectionId);
        }

        inspectionRepository.deleteById(inspectionId);
        log.info("Inspection deleted successfully with ID: {}", inspectionId);
    }

    @Transactional(readOnly = true)
    public InspectionResponse getInspectionByScheduleId(UUID scheduleId) {
        log.info("Getting inspection by schedule ID: {}", scheduleId);

        Inspection inspection = inspectionRepository.findByScheduleIdWithInsurance(scheduleId)
                .orElseThrow(() -> ApiException.notFound("Inspection not found for schedule ID: " + scheduleId));

        return getInspectionById(inspection.getId());
    }

    private CustomUserDetails getCurrentUserDetails() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new ApiException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        return (CustomUserDetails) authentication.getPrincipal();
    }

    private String getInspectorName(CustomUserDetails userDetails) {
        if (userDetails.getFirstName() != null && userDetails.getLastName() != null) {
            return userDetails.getFirstName() + " " + userDetails.getLastName();
        }
        return userDetails.getUsername();
    }
}
