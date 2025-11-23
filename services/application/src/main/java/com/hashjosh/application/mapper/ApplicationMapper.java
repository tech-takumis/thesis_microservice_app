package com.hashjosh.application.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashjosh.application.clients.DocumentServiceClient;
import com.hashjosh.application.clients.InsuranceClient;
import com.hashjosh.application.dto.submission.ApplicationSubmissionDto;
import com.hashjosh.application.model.Application;
import com.hashjosh.application.model.ApplicationType;
import com.hashjosh.constant.application.ApplicationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationMapper {
    private final DocumentServiceClient documentServiceClient;
    private final InsuranceClient insuranceClient;

    public ApplicationResponseDto toApplicationResponseDto(
            Application entity

    ) {
        List<String> documentUrls = entity.getDocuments().stream()
                .map(document -> documentServiceClient.generatePresignedUrl(entity.getUserId(), document.getDocumentId(), 60))
                .toList();

        String applicationStatus = insuranceClient.getInsuranceStatus(entity.getId(), entity.getUserId());


        ApplicationResponseDto.ApplicationResponseDtoBuilder builder = ApplicationResponseDto.builder()
                .id(entity.getId())
                .applicationTypeId(entity.getType().getId())
                .applicationTypeName(entity.getType().getName())
                .status(applicationStatus)
                .coordinates(entity.getCoordinates())
                .submittedAt(entity.getSubmittedAt())
                .fileUploads(documentUrls)
                .jsonDynamicFields(entity.getDynamicFields())
                .updatedAt(entity.getUpdatedAt())
                .version(entity.getVersion());

        return builder.build();
    }


    public Application toEntity(ApplicationSubmissionDto submission, ApplicationType type) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dynamicFieldsNode = objectMapper.valueToTree(submission.getFieldValues());

        return Application.builder()
                .type(type)
                .userId(submission.getUseId())
                .coordinates(submission.getCoordinates())
                .fullName(submission.getFullName())
                .dynamicFields(dynamicFieldsNode)
                .documents(submission.getDocuments())
                .submittedAt(LocalDateTime.now())
                .build();
    }


}
