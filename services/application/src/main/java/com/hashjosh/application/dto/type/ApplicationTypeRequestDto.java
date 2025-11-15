package com.hashjosh.application.dto.type;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hashjosh.application.dto.sections.ApplicationSectionRequestDto;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApplicationTypeRequestDto(
        String name,
        String description,
        String providerName,
        String layout,
        List<ApplicationSectionRequestDto> sections
) {
}
