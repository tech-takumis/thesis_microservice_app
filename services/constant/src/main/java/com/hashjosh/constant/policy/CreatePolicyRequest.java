package com.hashjosh.insurance.dto.policy;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePolicyRequest {

    @NotNull(message = "Insurance ID is required")
    private UUID insuranceId;

    @NotNull(message = "Effective date is required")
    private LocalDateTime effectiveDate;

    @NotNull(message = "Expiry date is required")
    private LocalDateTime expiryDate;
}
