package com.hashjosh.constant.policy;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyResponse {
    private UUID id;

    private UUID insuranceId;

    private String policyNumber;

    private LocalDateTime effectiveDate;

    private LocalDateTime expiryDate;
}
