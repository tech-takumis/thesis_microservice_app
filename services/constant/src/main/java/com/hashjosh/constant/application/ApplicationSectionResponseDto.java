package com.hashjosh.constant.application;

import java.util.List;
import java.util.UUID;

public record ApplicationSectionResponseDto(
        UUID id,
        String title,
        List<ApplicationFieldsResponseDto> fields
) {
}
