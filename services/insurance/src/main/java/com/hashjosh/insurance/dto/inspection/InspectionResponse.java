package com.hashjosh.insurance.dto.inspection;

import com.fasterxml.jackson.databind.JsonNode;
import com.hashjosh.constant.program.dto.ScheduleResponseDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionResponse {

    private UUID id;

    private UUID insuranceId;

    private UUID inspectorId;

    private String inspectorName;

    private LocalDateTime inspectedAt;

    private List<String> photos;

    private ScheduleResponseDto schedule;

    private JsonNode fieldValues;

}
