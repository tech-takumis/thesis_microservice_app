package com.hashjosh.insurance.dto.claim;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimRequest {

    private UUID insuranceId;

    private boolean isFinalized;

    private String damageAssessment;

    private JsonNode fieldValues;
}
