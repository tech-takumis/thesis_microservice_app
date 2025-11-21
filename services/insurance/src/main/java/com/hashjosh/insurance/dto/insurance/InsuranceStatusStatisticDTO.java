package com.hashjosh.insurance.dto.insurance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceStatusStatisticDTO {
    private String status;
    private long count;
}

