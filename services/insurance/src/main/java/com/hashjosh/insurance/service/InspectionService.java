package com.hashjosh.insurance.service;

import com.hashjosh.constant.pcic.enums.InsuranceStatus;
import com.hashjosh.constant.program.dto.ScheduleRequestDto;
import com.hashjosh.constant.program.dto.ScheduleResponseDto;
import com.hashjosh.insurance.clients.ScheduleClient;
import com.hashjosh.insurance.config.CustomUserDetails;
import com.hashjosh.insurance.dto.inspection.InspectionRequest;
import com.hashjosh.insurance.dto.inspection.InspectionResponse;
import com.hashjosh.insurance.entity.Inspection;
import com.hashjosh.insurance.entity.Insurance;
import com.hashjosh.insurance.exception.ApiException;
import com.hashjosh.insurance.kafka.KafkaProducer;
import com.hashjosh.insurance.mapper.InspectionMapper;
import com.hashjosh.insurance.repository.InspectionRepository;
import com.hashjosh.insurance.repository.InsuranceRepository;
import com.hashjosh.kafkacommon.application.InspectionCompletedEvent;
import com.hashjosh.kafkacommon.application.InspectionScheduledEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final KafkaProducer kafkaProducer;

    @Transactional
    public InspectionResponse createInspection(UUID insuranceId, InspectionRequest request, List<org.springframework.web.multipart.MultipartFile> photos) {
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

        // Create inspection entity
        Inspection inspection = inspectionMapper.toEntity(request, insurance,
                userDetails.getUserId(), getInspectorName(userDetails), photos);

        // Save inspection
        Inspection savedInspection = inspectionRepository.save(inspection);
        insurance.setCurrentStatus(InsuranceStatus.INSPECTION_COMPLETED);
        insuranceRepository.save(insurance);

        InspectionCompletedEvent event = InspectionCompletedEvent.builder()
                .submissionId(insurance.getSubmissionId())
                .userId(insurance.getFarmerId())
                .inspectorName(getInspectorName(userDetails))
                .status(insurance.getCurrentStatus().name())
                .inspectedAt(savedInspection.getInspectedAt())
                .build();

        kafkaProducer.publishEvent("application-inspection-completed",event);

        log.info("Inspection created successfully with ID: {}", savedInspection.getId());
        return inspectionMapper.toResponse(savedInspection);
    }

    public ScheduleResponseDto createInspectionSchedule(UUID insuranceId, @Valid ScheduleRequestDto request) {
        log.info("Creating inspection schedule for insurance ID: {}", insuranceId);

        Insurance insurance = insuranceRepository.findById(insuranceId)
                .orElseThrow(() -> ApiException.notFound("Insurance not found with ID: " + insuranceId));

        // Get current user from security context
        CustomUserDetails userDetails = getCurrentUserDetails();

        // Call ScheduleClient to create schedule
        ScheduleResponseDto response = scheduleClient.createSchedule(request, userDetails.getUserId());

        Inspection inspection = inspectionMapper.toEntity(response,insurance,userDetails.getUserId(), getInspectorName(userDetails));

        inspectionRepository.save(inspection);

        insurance.setCurrentStatus(InsuranceStatus.SCHEDULE_ASSIGNED_FOR_INSPECTION);
        insuranceRepository.save(insurance);

        InspectionScheduledEvent event = InspectionScheduledEvent.builder()
                .submissionId(insurance.getSubmissionId())
                .insuranceId(insuranceId)
                .farmerId(insurance.getFarmerId())
                .farmernName(insurance.getFarmerName())
                .status(insurance.getCurrentStatus().name())
                .scheduleId(response.getId())
                .scheduledAt(response.getScheduleDate())
                .build();

        kafkaProducer.publishEvent("application-inspection-schedule", event);

        log.info("Inspection schedule created successfully with ID: {}", response.getId());
        return response;
    }

    @Transactional(readOnly = true)
    public InspectionResponse getInspectionById(UUID inspectionId) {
        log.info("Getting inspection by ID: {}", inspectionId);

        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> ApiException.notFound("Inspection not found with ID: " + inspectionId));

        return inspectionMapper.toResponse(inspection);
    }

    @Transactional(readOnly = true)
    public InspectionResponse getInspectionByInsuranceId(UUID insuranceId) {
        log.info("Getting inspection by insurance ID: {}", insuranceId);

        Inspection inspection = inspectionRepository.findByInsurance_Id(insuranceId)
                .orElseThrow(() -> ApiException.notFound("Inspection not found for insurance ID: " + insuranceId));

        return getInspectionById(inspection.getId());
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
    public InspectionResponse updateInspection(UUID inspectionId, InspectionRequest request, List<MultipartFile> photos) {
        log.info("Updating inspection with ID: {}", inspectionId);

        // Get current user from security context
        CustomUserDetails userDetails = getCurrentUserDetails();

        // Find existing inspection
        Inspection existingInspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> ApiException.notFound("Inspection not found with ID: " + inspectionId));

        // Update inspection entity
        inspectionMapper.updateEntity(existingInspection, request,
                userDetails.getUserId(), getInspectorName(userDetails), photos);

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
