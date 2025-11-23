package com.hashjosh.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationDetailDTO {

    private UUID applicationId;
    private String applicationTypeName;
    private String applicationTypeDescription;
    private boolean printable;
    private Boolean requiredAIAnalysis;
    private String status;
    private String coordinates;
    private JsonNode dynamicFields;
    private List<String> documentUrls;
}
