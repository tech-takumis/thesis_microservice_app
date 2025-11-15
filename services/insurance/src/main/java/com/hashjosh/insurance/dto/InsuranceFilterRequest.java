package com.hashjosh.insurance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceFilterRequest {
    private Boolean batch;
    private Boolean application;
    private Boolean farmer;
    private Boolean verification;
    private Boolean inspection;
    private Boolean policy;
    private Boolean claim;

    // Convert to InsuranceFilter
    public InsuranceFilter toInsuranceFilter() {
        return new InsuranceFilter(batch, application, farmer, verification, inspection, policy, claim);
    }
}
