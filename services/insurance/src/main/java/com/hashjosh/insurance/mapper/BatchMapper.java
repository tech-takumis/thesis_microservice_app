package com.hashjosh.insurance.mapper;

import com.hashjosh.insurance.dto.batch.BatchRequestDTO;
import com.hashjosh.insurance.dto.batch.BatchResponseDTO;
import com.hashjosh.insurance.entity.Batch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchMapper {


    public Batch toEntity(BatchRequestDTO request) {
        return Batch.builder()
                .name(request.getName())
                .applicationTypeId(request.getApplicationTypeId())
                .description(request.getDescription())
                .isAvailable(request.isAvailable())
                .maxApplications(request.getMaxApplications())
                .endDate(request.getEndDate())
                .startDate(request.getStartDate())
                .build();
    }

    public BatchResponseDTO toBatchResponseDTO(Batch save) {

        return BatchResponseDTO.builder()
                .id(save.getId())
                .applicationTypeId(save.getApplicationTypeId())
                .batchName(save.getName())
                .description(save.getDescription())
                .maxApplications(save.getMaxApplications())
                .totalApplications(save.getTotalApplications())
                .isAvailable(save.isAvailable())
                .endDate(save.getEndDate())
                .startDate(save.getStartDate())
                .build();
    }
}
