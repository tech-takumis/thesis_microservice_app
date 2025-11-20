package com.hashjosh.insurance.mapper;

import com.hashjosh.constant.policy.CreatePolicyRequest;
import com.hashjosh.constant.policy.PolicyResponse;
import com.hashjosh.constant.policy.UpdatePolicyRequest;
import com.hashjosh.insurance.entity.Insurance;
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

    public Policy toEntity(CreatePolicyRequest request, Insurance insurance) {
        return Policy.builder()
                .insurance(insurance)
                .effectiveDate(request.getEffectiveDate())
                .expiryDate(request.getExpiryDate())
                .build();
    }

    public void updateEntity(Policy policy, UpdatePolicyRequest request) {
        if (request.getPolicyNumber() != null) {
            policy.setPolicyNumber(request.getPolicyNumber());
        }
        if (request.getEffectiveDate() != null) {
            policy.setEffectiveDate(request.getEffectiveDate());
        }
        if (request.getExpiryDate() != null) {
            policy.setExpiryDate(request.getExpiryDate());
        }
    }
}
