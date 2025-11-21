package com.hashjosh.application.mapper;

import com.hashjosh.application.dto.type.ApplicationTypeRequestDto;
import com.hashjosh.application.model.Application;
import com.hashjosh.application.model.ApplicationType;
import com.hashjosh.application.model.ApplicationWorkflow;
import com.hashjosh.constant.application.ApplicationResponseDto;
import com.hashjosh.constant.application.ApplicationTypeResponseDto;
import com.hashjosh.constant.workflow.ApplicationWorkflowResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApplicationTypeMapper {

    private final ApplicationMapper applicationMapper;
    private final ApplicationSectionMapper applicationSectionMapper;

    public ApplicationType toApplicationType(ApplicationTypeRequestDto dto) {
        return ApplicationType.builder()
                .name(dto.name())
                .description(dto.description())
                .layout(dto.layout())
                .build();
    }

    public ApplicationTypeResponseDto toApplicationResponse(
            ApplicationType applicationType,
            Boolean includeApplicationResponse,
            Boolean includeSections
    ) {

        List<ApplicationResponseDto> applications = new ArrayList<>();

        if(applicationType.getApplications() != null) {
            for (Application app : applicationType.getApplications()) {
                applications.add(applicationMapper.toApplicationResponseDto(app));
            }
        }

        ApplicationTypeResponseDto responseDto = ApplicationTypeResponseDto.builder()
                .id(applicationType.getId())
                .name(applicationType.getName())
                .description(applicationType.getDescription())
                .provider(applicationType.getProvider().getName())
                .layout(applicationType.getLayout())
                .requiresAIAnalysis(applicationType.getRequiredAIAnalysis())
                .printable(applicationType.getPrintable())
                .workflow(mapToApplicationWorkflowResponse(applicationType.getApplicationWorkflow()))
                .build();

        if((includeApplicationResponse != null && includeApplicationResponse) && !applications.isEmpty()) {
            responseDto.setApplications(applications);
        }

        if((includeSections != null && includeSections) && applicationType.getSections() != null) {
            responseDto.setSections(applicationType.getSections().stream()
                    .map(applicationSectionMapper::toApplicationSectionResponseDto)
                    .toList());
        }

        return responseDto;
    }

    private ApplicationWorkflowResponse mapToApplicationWorkflowResponse(ApplicationWorkflow applicationWorkflow) {
        return ApplicationWorkflowResponse.builder()
                .id(applicationWorkflow.getId())
                .claim_enabled(applicationWorkflow.isClaimEnabled())
                .verification_enabled(applicationWorkflow.isVerificationEnabled())
                .inspection_enabled(applicationWorkflow.isInspectionEnabled())
                .policy_enabled(applicationWorkflow.isPolicyEnabled())
                .createdAt(applicationWorkflow.getCreatedAt())
                .updatedAt(applicationWorkflow.getUpdatedAt())
                .build();
    }


}
