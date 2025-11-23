package com.hashjosh.insurance.dto.claim;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimResponse {

    private UUID id;

    private UUID insuranceId;

    private UUID farmerId;

    private String farmerName;

    private LocalDateTime filedAt;

    private boolean isFinalized;

    private String damageAssessment;

    private Double claimAmount;

    private List<String> supportingFiles;

    private JsonNode fieldValues;
}
