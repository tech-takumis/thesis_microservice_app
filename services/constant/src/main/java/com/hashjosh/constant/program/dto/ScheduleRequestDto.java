package com.hashjosh.constant.program.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.hashjosh.constant.program.enums.SchedulePriority;
import com.hashjosh.constant.program.enums.ScheduleType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequestDto {
    private ScheduleType type;
    private LocalDateTime scheduleDate;
    private SchedulePriority priority;
    private String notes;
}
