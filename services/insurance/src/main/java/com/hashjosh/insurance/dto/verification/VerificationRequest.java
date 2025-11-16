package com.hashjosh.insurance.dto.verification;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationRequest {

    @NotBlank(message = "Remarks cannot be blank")
    private String remarks;

    @NotNull(message = "Field values cannot be null")
    private JsonNode fieldValues;
}
