package com.hashjosh.insurance.dto.claim;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimAIRequest {

    private UUID insuranceId;

    private boolean isFinalized;

    private String applicationId;
}
