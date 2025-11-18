package com.hashjosh.insurance.dto.verification;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;
import java.util.UUID;


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

    private List<UUID> verificationDocuments;
}
