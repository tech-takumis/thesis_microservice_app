package com.hashjosh.application.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashjosh.application.clients.DocumentServiceClient;
import com.hashjosh.application.clients.FarmerServiceClient;
import com.hashjosh.application.dto.submission.ApplicationSubmissionDto;
import com.hashjosh.application.mapper.ApplicationTypeMapper;
import com.hashjosh.application.model.Document;
import com.hashjosh.application.model.Application;
import com.hashjosh.application.model.ApplicationType;
import com.hashjosh.constant.application.ApplicationResponseDto;
import com.hashjosh.constant.application.ApplicationTypeResponseDto;
import com.hashjosh.constant.farmer.FarmerReponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationMapper {

    private final DocumentServiceClient documentServiceClient;
    private final FarmerServiceClient farmerServiceClient;
    private final ApplicationTypeMapper applicationTypeMapper;

    public ApplicationResponseDto toApplicationResponseDto(
            Application entity,
            Boolean includeApplicationType,
            Boolean includeFarmer,
            Boolean includeFieldValues,
            Boolean includeFileUploads
    ) {
        ApplicationResponseDto.ApplicationResponseDtoBuilder builder = ApplicationResponseDto.builder()
                .id(entity.getId())
                .submittedAt(entity.getSubmittedAt())
                .updatedAt(entity.getUpdatedAt())
                .version(entity.getVersion());

        // Conditionally include application type
        if (Boolean.TRUE.equals(includeApplicationType) && entity.getType() != null) {
            ApplicationTypeResponseDto applicationTypeDto = applicationTypeMapper
                    .toApplicationResponse(entity.getType(), false, false);
            builder.applicationType(applicationTypeDto);
        }

        // Conditionally include farmer information
        if (Boolean.TRUE.equals(includeFarmer) && entity.getUserId() != null) {
            try {
                FarmerReponse farmer = farmerServiceClient.getFarmerById(entity.getUserId(), entity.getUserId().toString());
                builder.farmer(farmer);
            } catch (Exception e) {
                log.warn("Failed to fetch farmer data for user {}: {}", entity.getUserId(), e.getMessage());
                // Continue without farmer data
            }
        }

        // Conditionally include file uploads
        if( Boolean.TRUE.equals(includeFileUploads)) {
            List<String> fileUrls = new ArrayList<>();
            if (entity.getDocuments() != null) {
                for (Document document : entity.getDocuments()) {
                    try {
                        String url = documentServiceClient.generatePresignedUrl(
                                String.valueOf(entity.getUserId()),
                                document.getDocumentId(),
                                30
                        );
                        fileUrls.add(url);
                    } catch (Exception e) {
                        log.warn("Failed to generate presigned URL for document {}: {}", document.getDocumentId(), e.getMessage());
                    }
                }
            }
            builder.fileUploads(fileUrls);
        }

        // Conditionally include field values
        if (Boolean.TRUE.equals(includeFieldValues) && entity.getDynamicFields() != null) {
            builder.jsonDynamicFields(entity.getDynamicFields());
        }

        return builder.build();
    }


    public Application toEntity(ApplicationSubmissionDto submission, ApplicationType type) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dynamicFieldsNode = objectMapper.valueToTree(submission.getFieldValues());

        return Application.builder()
                .type(type)
                .userId(submission.getUseId())
                .fullName(submission.getFullName())
                .dynamicFields(dynamicFieldsNode)
                .documents(submission.getDocuments())
                .submittedAt(LocalDateTime.now())
                .build();
    }
}
