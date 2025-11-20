package com.hashjosh.insurance.dto.inspection;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionRequest {

    private JsonNode fieldValues;

}
