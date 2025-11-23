package com.hashjosh.constant.application;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.hashjosh.constant.ai.AIResultDTO;
import com.hashjosh.constant.farmer.FarmerReponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationResponseDto{
    UUID id;
    UUID applicationTypeId;
    String applicationTypeName;
    String status;
    String coordinates;
    List<String> fileUploads;
    @JsonProperty("dynamicFields")
    JsonNode jsonDynamicFields;
    LocalDateTime  submittedAt;
    LocalDateTime updatedAt;
    Long version;
}
