package com.hashjosh.insurance.dto.batch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hashjosh.constant.application.ApplicationTypeResponseDto;
import com.hashjosh.insurance.dto.insurance.InsuranceResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class BatchResponseDTO {
    UUID id;
    String batchName;
    String description;
    boolean isAvailable;
    int totalApplications;
    int maxApplications;
    LocalDateTime startDate;
    LocalDateTime endDate;
    List<InsuranceResponse> insurances;
}
