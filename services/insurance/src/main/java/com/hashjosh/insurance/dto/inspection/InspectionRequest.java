package com.hashjosh.insurance.dto.inspection;

import com.fasterxml.jackson.databind.JsonNode;
import com.hashjosh.constant.program.enums.SchedulePriority;
import com.hashjosh.constant.program.enums.ScheduleType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionRequest {

    private List<String> photos;

    private JsonNode fieldValues;

    // For scheduling inspection
    private ScheduleType type;
    private LocalDateTime scheduleDate;
    private SchedulePriority priority;
    private JsonNode metaData;
}
