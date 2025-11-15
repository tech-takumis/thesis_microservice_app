package com.hashjosh.insurance.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceFilter {

    private Boolean batch;
    private Boolean application;
    private Boolean farmer;
    private Boolean verification;
    private Boolean inspection;
    private Boolean claim;
    private Boolean policy;

}
