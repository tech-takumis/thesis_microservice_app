package com.hashjosh.insurance.dto.verification;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationResponse {

    private UUID id;

    private UUID insuranceId;

    private UUID verifierId;

    private String verifierName;

    private String remarks;

    private JsonNode fieldValues;

    private List<UUID> verificationDocuments;

    private LocalDateTime verifiedAt;
}
