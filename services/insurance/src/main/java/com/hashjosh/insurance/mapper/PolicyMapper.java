package com.hashjosh.insurance.mapper;

import com.hashjosh.insurance.dto.policy.PolicyResponse;
import com.hashjosh.insurance.entity.Policy;
import org.springframework.stereotype.Component;

@Component
public class PolicyMapper {
    public PolicyResponse toResponse(Policy policy) {
        return PolicyResponse.builder()
                .id(policy.getId())
                .insuranceId(policy.getInsurance().getId())
                .policyNumber(policy.getPolicyNumber())
                .effectiveDate(policy.getEffectiveDate())
                .expiryDate(policy.getExpiryDate())
                .build();
    }
}
