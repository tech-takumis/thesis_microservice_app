package com.hashjosh.insurance.mapper;

import com.hashjosh.constant.document.dto.DocumentResponse;
import com.hashjosh.constant.program.dto.ScheduleResponseDto;
import com.hashjosh.insurance.clients.DocumentServiceClient;
import com.hashjosh.insurance.clients.ScheduleClient;
import com.hashjosh.insurance.dto.inspection.InspectionRequest;
import com.hashjosh.insurance.dto.inspection.InspectionResponse;
import com.hashjosh.insurance.entity.Inspection;
import com.hashjosh.insurance.entity.Insurance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InspectionMapper {

    private final DocumentServiceClient documentClient;
    private final ScheduleClient scheduleClient;

    public Inspection toEntity(InspectionRequest request, Insurance insurance, UUID inspectorId, String inspectorName, List<org.springframework.web.multipart.MultipartFile> photos) {
        return Inspection.builder()
                .inspectorId(inspectorId)
                .inspectorName(inspectorName)
                .inspectedAt(LocalDateTime.now())
                .fieldValues(request.getFieldValues())
                .inspected(true)
                .photos(uploadPhotosAndGetIds(photos, inspectorId))
                .insurance(insurance)
                .build();
    }

    public Inspection toEntity(ScheduleResponseDto responseDto, Insurance insurance, UUID inspectorId, String inspectorName) {
        return Inspection.builder()
                .inspectorId(inspectorId)
                .inspectorName(inspectorName)
                .inspectedAt(LocalDateTime.now())
                .fieldValues(null)
                .photos(null)
                .inspected(false)
                .scheduleId(responseDto.getId())
                .insurance(insurance)
                .build();
    }

    public InspectionResponse toResponse(Inspection inspection) {
        return InspectionResponse.builder()
                .id(inspection.getId())
                .insuranceId(inspection.getInsurance().getId())
                .inspectorId(inspection.getInspectorId())
                .schedule(getInspectionSchedule(inspection.getScheduleId(), inspection.getInspectorId()))
                .inspectorName(inspection.getInspectorName())
                .isInspected(inspection.isInspected())
                .inspectedAt(inspection.getInspectedAt())
                .photos(convertPhotosToStrings(inspection.getPhotos(), inspection.getInspectorId(), 60))
                .fieldValues(inspection.getFieldValues())
                .build();
    }

    public void updateEntity(Inspection existingInspection, InspectionRequest request, UUID inspectorId, String inspectorName, List<MultipartFile> photos) {
        existingInspection.setInspectorId(inspectorId);
        existingInspection.setInspectorName(inspectorName);
        existingInspection.setInspectedAt(LocalDateTime.now());
        existingInspection.setFieldValues(request.getFieldValues());
        existingInspection.setInspected(true);

        // Only update photos if new ones are provided
        if (photos != null && !photos.isEmpty()) {
            existingInspection.setPhotos(uploadPhotosAndGetIds(photos, inspectorId));
        }
    }

    private List<UUID> uploadPhotosAndGetIds(List<MultipartFile> photos, UUID userId) {
        if (photos == null || photos.isEmpty()) {
            return new ArrayList<>();
        }

        List<UUID> documentIds = new ArrayList<>();
        for (MultipartFile photo : photos) {
            try {
                log.info("Uploading photo: {} for user: {}", photo.getOriginalFilename(), userId);
                DocumentResponse documentResponse = documentClient.uploadDocument(photo, userId.toString());
                documentIds.add(documentResponse.getDocumentId());
                log.info("Photo uploaded successfully with document ID: {}", documentResponse.getDocumentId());
            } catch (Exception e) {
                log.error("Failed to upload photo: {} for user: {}", photo.getOriginalFilename(), userId, e);
                throw new RuntimeException("Failed to upload photo: " + photo.getOriginalFilename(), e);
            }
        }
        return documentIds;
    }

    private List<String> convertPhotosToStrings(List<UUID> photos,UUID userId,int expiryInMinutes) {
        if (photos == null) return null;
        return photos.stream()
                .map(id -> {
                    return documentClient.generatePresignedUrl(userId, id, expiryInMinutes);
                })
                .toList();
    }

    private ScheduleResponseDto getInspectionSchedule(UUID scheduleId,UUID userId) {
        return scheduleClient.getInspectionScheduleById(scheduleId,userId);
    }
}
