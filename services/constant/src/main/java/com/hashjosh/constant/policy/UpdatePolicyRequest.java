package com.hashjosh.constant.policy;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePolicyRequest {

    private String policyNumber;

    private LocalDateTime effectiveDate;

    private LocalDateTime expiryDate;
}
