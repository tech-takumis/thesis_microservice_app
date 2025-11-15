package com.hashjosh.constant.ai;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
class Top3PredictionDTO {
    private String class_name;
    private String confidence;
    private int rank;
}