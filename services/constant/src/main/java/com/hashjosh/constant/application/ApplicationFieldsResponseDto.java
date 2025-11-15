package com.hashjosh.constant.application;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public record ApplicationFieldsResponseDto(
        UUID id,
        String key,
        String fieldName,
        FieldType fieldType,
        Boolean required,
        String defaultValue,
        JsonNode choices,
        String validationRegex,
        Boolean requiredAIAnalysis
) {
}
