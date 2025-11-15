package com.hashjosh.constant.application;

import java.util.List;
import java.util.UUID;

public record ApplicationTypeResponseDto(
        UUID id,
        String name,
        String description,
        String provider,
        String layout,
        Boolean printable,
        List<ApplicationSectionResponseDto> sections
) {
}
