package com.hashjosh.kafkacommon.application;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InspectionScheduledEvent {
    private UUID insuranceId;
    private UUID submissionId;
    private UUID farmerId;
    private String farmernName;
    private String status;
    private UUID scheduleId;
    private LocalDateTime scheduledAt;
}
