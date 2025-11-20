package com.hashjosh.kafkacommon.application;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCompletedEvent {
    private UUID submissionId;
    private UUID userId;
    private String verifierName;
    private String status;
    private String remarks ;
    private LocalDateTime verifiedAt;

}

/*
* VerificationCompletedEvent {
  "submissionId": UUID, // Application.id
  "userId": UUID,
  "status": "PENDING|APPROVED|REJECTED",
  "rejectionReason": String (optional),
  "verifiedAt": Timestamp
}
*
**/