package com.hashjosh.insurance.dto.claim;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimUpdateRequest {

    @DecimalMin(value = "0.01", message = "Claim amount must be greater than 0")
    private Double claimAmount;

    @Size(max = 2000, message = "Damage assessment cannot exceed 2000 characters")
    private String damageAssessment;
}
