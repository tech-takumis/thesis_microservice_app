package com.hashjosh.insurance.mapper;

import com.hashjosh.constant.program.dto.ScheduleResponseDto;
import com.hashjosh.insurance.clients.DocumentServiceClient;
import com.hashjosh.insurance.dto.inspection.InspectionRequest;
import com.hashjosh.insurance.dto.inspection.InspectionResponse;
import com.hashjosh.insurance.entity.Inspection;
import com.hashjosh.insurance.entity.Insurance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InspectionMapper {

    private final DocumentServiceClient documentClient;

    public Inspection toEntity(InspectionRequest request, Insurance insurance, UUID inspectorId, String inspectorName) {
        return Inspection.builder()
                .inspectorId(inspectorId)
                .inspectorName(inspectorName)
                .inspectedAt(LocalDateTime.now())
                .fieldValues(request.getFieldValues())
                .photos(convertPhotosToUUIDs(request.getPhotos()))
                .insurance(insurance)
                .build();
    }

    public InspectionResponse toResponse(Inspection inspection, ScheduleResponseDto schedule) {
        return InspectionResponse.builder()
                .id(inspection.getId())
                .insuranceId(inspection.getInsurance().getId())
                .inspectorId(inspection.getInspectorId())
                .inspectorName(inspection.getInspectorName())
                .inspectedAt(inspection.getInspectedAt())
                .photos(convertPhotosToStrings(inspection.getPhotos(), String.valueOf(inspection.getInspectorId()), 60))
                .fieldValues(inspection.getFieldValues())
                .schedule(schedule)
                .build();
    }

    public InspectionResponse toResponse(Inspection inspection) {
        return toResponse(inspection, null);
    }

    public void updateEntity(Inspection existingInspection, InspectionRequest request, UUID inspectorId, String inspectorName) {
        existingInspection.setInspectorId(inspectorId);
        existingInspection.setInspectorName(inspectorName);
        existingInspection.setInspectedAt(LocalDateTime.now());
        existingInspection.setFieldValues(request.getFieldValues());
        existingInspection.setPhotos(convertPhotosToUUIDs(request.getPhotos()));
    }

    private List<UUID> convertPhotosToUUIDs(List<String> photos) {
        if (photos == null) return null;
        return photos.stream()
                .map(UUID::fromString)
                .toList();
    }

    private List<String> convertPhotosToStrings(List<UUID> photos,String userId,int expiryInMinutes) {
        if (photos == null) return null;
        return photos.stream()
                .map(id -> {
                    return documentClient.generatePresignedUrl(userId, id, expiryInMinutes);
                })
                .toList();
    }
}
