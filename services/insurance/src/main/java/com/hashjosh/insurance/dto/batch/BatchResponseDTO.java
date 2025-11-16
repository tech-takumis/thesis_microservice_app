package com.hashjosh.insurance.dto.batch;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class BatchResponseDTO {
    UUID id;
    String batchName;
    private UUID applicationTypeId;
    String description;
    boolean isAvailable;
    int totalApplications;
    int maxApplications;
    LocalDateTime startDate;
    LocalDateTime endDate;
}
