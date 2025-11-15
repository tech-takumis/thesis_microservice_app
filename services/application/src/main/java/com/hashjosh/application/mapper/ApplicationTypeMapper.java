package com.hashjosh.application.mapper;

import com.hashjosh.application.dto.type.ApplicationTypeRequestDto;
import com.hashjosh.constant.application.ApplicationSectionResponseDto;
import com.hashjosh.constant.application.ApplicationFieldsResponseDto;
import com.hashjosh.constant.application.ApplicationTypeResponseDto;
import com.hashjosh.application.model.ApplicationType;
import com.hashjosh.application.model.ApplicationSection;
import com.hashjosh.application.model.ApplicationField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ApplicationTypeMapper {

    public ApplicationType toApplicationType(ApplicationTypeRequestDto dto) {
        return ApplicationType.builder()
                .name(dto.name())
                .description(dto.description())
                .layout(dto.layout())
                .build();
    }

    public ApplicationTypeResponseDto toApplicationResponse(ApplicationType applicationType, Boolean includeSections, Boolean includeFields) {
        List<ApplicationSectionResponseDto> sections = null;

        if (Boolean.TRUE.equals(includeSections) && applicationType.getSections() != null) {
            sections = applicationType.getSections().stream()
                    .map(section -> mapSection(section, includeFields))
                    .toList();
        }

        return new ApplicationTypeResponseDto(
                applicationType.getId(),
                applicationType.getName(),
                applicationType.getDescription(),
                applicationType.getProvider().getName(),
                applicationType.getLayout(),
                applicationType.getPrintable(),
                sections
        );
    }

    private ApplicationSectionResponseDto mapSection(ApplicationSection section, Boolean includeFields) {
        List<ApplicationFieldsResponseDto> fields = null;

        if (Boolean.TRUE.equals(includeFields) && section.getFields() != null) {
            fields = section.getFields().stream()
                    .map(this::mapField)
                    .toList();
        }

        return new ApplicationSectionResponseDto(
                section.getId(),
                section.getTitle(),
                fields
        );
    }

    private ApplicationFieldsResponseDto mapField(ApplicationField field) {
        return new ApplicationFieldsResponseDto(
                field.getId(),
                field.getKey(),
                field.getFieldName(),
                com.hashjosh.constant.application.FieldType.valueOf(field.getFieldType().name()),
                field.getRequired(),
                field.getDefaultValue(),
                field.getChoices(),
                field.getValidationRegex(),
                field.getRequiredAIAnalysis()
        );
    }
}
