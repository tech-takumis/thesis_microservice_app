package com.hashjosh.constant.program.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.hashjosh.constant.program.enums.SchedulePriority;
import com.hashjosh.constant.program.enums.ScheduleType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponseDto {
    private UUID id;
    private ScheduleType type;
    private LocalDateTime scheduleDate;
    private SchedulePriority priority;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
