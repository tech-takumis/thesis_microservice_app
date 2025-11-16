package com.hashjosh.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashjosh.application.clients.DocumentServiceClient;
import com.hashjosh.application.clients.AIClient;
import com.hashjosh.application.clients.FarmerServiceClient;
import com.hashjosh.application.configs.CustomUserDetails;
import com.hashjosh.application.dto.submission.ApplicationSubmissionDto;
import com.hashjosh.application.dto.validation.ValidationError;
import com.hashjosh.application.dto.validation.ValidationErrors;
import com.hashjosh.application.exceptions.ApiException;
import com.hashjosh.application.kafka.ApplicationProducer;
import com.hashjosh.application.mapper.ApplicationMapper;
import com.hashjosh.application.model.Application;
import com.hashjosh.application.model.ApplicationField;
import com.hashjosh.application.model.ApplicationType;
import com.hashjosh.application.model.Document;
import com.hashjosh.application.repository.ApplicationRepository;
import com.hashjosh.application.repository.ApplicationTypeRepository;
import com.hashjosh.application.repository.DocumentRepository;
import com.hashjosh.application.validators.FieldValidatorFactory;
import com.hashjosh.application.validators.ValidatorStrategy;
import com.hashjosh.constant.ai.AIResultDTO;
import com.hashjosh.constant.application.ApplicationResponseDto;
import com.hashjosh.constant.document.dto.DocumentResponse;
import com.hashjosh.constant.farmer.FarmerReponse;
import com.hashjosh.kafkacommon.application.ApplicationSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final FieldValidatorFactory fieldValidatorFactory;
    private final ApplicationTypeRepository applicationTypeRepository;
    private final ApplicationMapper applicationMapper;
    private final ApplicationProducer  applicationProducer;
    private final DocumentServiceClient documentServiceClient;
    private final DocumentRepository documentRepository;
    private final AIClient aiClient;
    private final FarmerServiceClient farmerClient;

    @Transactional(propagation = Propagation.REQUIRED)
    public Application processSubmission(
            ApplicationSubmissionDto submission,
            Map<String, MultipartFile> fileMap
    ) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();

            submission.setUseId(UUID.fromString(userDetails.getUserId()));
            // We get the application type through the batch
            ApplicationType applicationType = applicationTypeRepository.findById(submission.getApplicationTypeId())
                    .orElseThrow(() -> ApiException.badRequest("Invalid application type ID"));

            // Upload files first and create Document entities
            List<Document> documents = new ArrayList<>();
            List<String> aiAnalysisFieldKey = applicationType.getSections().stream()
                    .flatMap(section -> section.getFields().stream())
                    .filter(field -> Boolean.TRUE.equals(field.getRequiredAIAnalysis()))
                    .map(ApplicationField::getKey)
                    .toList();

            List<String> objectKeysForAIAnalysis = new ArrayList<>();

            if (fileMap != null && !fileMap.isEmpty()) {
                for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
                    String documentKey = entry.getKey();
                    MultipartFile file = entry.getValue();

                    DocumentResponse docResponse = documentServiceClient.uploadDocument(file, userDetails.getUserId());
                    Document document = Document.builder()
                            .documentId(docResponse.getDocumentId())
                            .fileName(docResponse.getFileName())
                            .fileType(docResponse.getFileType())
                            .coordinates(submission.getCoordinates())
                            .uploadedAt(docResponse.getUploadedAt())
                            .documentKey(documentKey)
                            .objectKey(docResponse.getObjectKey())
                            .build();

                    documentRepository.save(document);

                    if(aiAnalysisFieldKey.contains(documentKey)) {
                        objectKeysForAIAnalysis.add(document.getObjectKey());
                    }
                    documents.add(document);
                }
            }
            submission.setDocuments(documents);

            List<ApplicationField> fields = applicationType.getSections().stream()
                    .flatMap(section -> section.getFields().stream())
                    .collect(Collectors.toList());

            List<ValidationError> validationErrors = validateSubmission(submission, fields);

            if (!validationErrors.isEmpty()) {
                throw ApiException.badRequest("Validation failed: " + validationErrors);
            }

            FarmerReponse farmer = getFarmerInfo(submission.getUseId(), submission.getUseId());
            submission.setFullName(farmer.getFirstName() + " " + farmer.getLastName());
            Application application = applicationMapper.toEntity(submission, applicationType);
            Application savedApplication = applicationRepository.save(application);

            applicationProducer.publishEvent("application-submitted",
                    ApplicationSubmittedEvent.builder()
                            .submissionId(savedApplication.getId())
                            .applicationTypeId(savedApplication.getType().getId())
                            .fullName(application.getFullName())
                            .provider(applicationType.getProvider().getName())
                            .objectKeysForAIAnalysis(objectKeysForAIAnalysis)
                            .documentIds(documents.stream().map(Document::getDocumentId).collect(Collectors.toList()))
                            .userId(savedApplication.getUserId())
                            .submittedAt(LocalDateTime.now())
                            .build()
            );

            return application;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw ApiException.internalError("An error occurred while processing your application: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ApplicationResponseDto getApplicationById(
            UUID applicationId
    ) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> ApiException.notFound("Application not found with id "+ applicationId));

        return applicationMapper.toApplicationResponseDto(application);
    }


    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> findAll(
    ) {

        return applicationRepository.findAll()
                .stream()
                .map(applicationMapper::toApplicationResponseDto)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> findAllApplication(
            String provider
    ) {

        ApplicationType type = applicationTypeRepository.findByProvider_Name(provider)
                .orElseThrow(() -> ApiException.notFound("Application type not found for provider " + provider));

        List<Application> applications = applicationRepository.findAllByApplicationTypeId(type.getId());

        return applications.stream()
                .map(applicationMapper::toApplicationResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteApplication(UUID applicationId) {
        Application application = findApplicationById(applicationId);
        applicationRepository.delete(application);
    }

    @Transactional
    public void updateApplicationDocuments(UUID applicationId, List<MultipartFile> files) {
        try {
            // Get current user details
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();

            // Find the existing application
            Application application = findApplicationById(applicationId);

            // Validate that files are provided
            if (files == null || files.isEmpty()) {
                throw ApiException.badRequest("No files provided for document update");
            }

            // Get existing coordinates from the first document if available
            String coordinates = null;
            if (!application.getDocuments().isEmpty()) {
                coordinates = application.getDocuments().get(0).getCoordinates();
            }

            // Upload new files and create Document entities
            List<Document> newDocuments = new ArrayList<>();
            for (MultipartFile file : files) {
                // Upload document using DocumentServiceClient
                var docResponse = documentServiceClient.uploadDocument(file, userDetails.getUserId());

                // Create new Document entity
                Document document = Document.builder()
                        .documentId(docResponse.getDocumentId())
                        .fileName(docResponse.getFileName())
                        .fileType(docResponse.getFileType())
                        .coordinates(coordinates) // Use existing coordinates or null
                        .uploadedAt(docResponse.getUploadedAt())
                        .build();

                // Save the document entity
                documentRepository.save(document);
                newDocuments.add(document);
            }

            // Replace old documents with new ones
            application.setDocuments(newDocuments);

            // Save the updated application
            applicationRepository.save(application);

            log.info("Successfully updated documents for application {}: {} new documents",
                    applicationId, newDocuments.size());

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update documents for application {}: {}", applicationId, e.getMessage(), e);
            throw ApiException.internalError("Failed to update application documents: " + e.getMessage());
        }
    }


    private List<ValidationError> validateSubmission(
            ApplicationSubmissionDto submission,
            List<ApplicationField> fields) {

        List<ValidationError> errors = new ArrayList<>();

        // 1. Check required fields
        fields.stream()
                .filter(ApplicationField::getRequired)
                .filter(field -> !submission.getFieldValues().containsKey(field.getKey()))
                .forEach(field -> errors.add(new ValidationError(
                        field.getKey(),
                        String.format("Field '%s' is required", field.getFieldName())
                )));

        // 2. Validate field types and constraints
        submission.getFieldValues().forEach((key, value) -> {
            fields.stream()
                    .filter(field -> field.getKey().equals(key))
                    .findFirst()
                    .ifPresent(field -> {
                        try {
                            // Get the appropriate validator for the field type
                            ValidatorStrategy validator = fieldValidatorFactory.getStrategy(field.getFieldType().name());
                            // Convert value to JsonNode and validate the field value
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode valueNode = objectMapper.valueToTree(value);
                            List<ValidationErrors> fieldErrors = validator.validate(field, valueNode);
                            if (fieldErrors != null && !fieldErrors.isEmpty()) {
                                // Since ValidationErrors is being used as a single error, we'll treat it as such
                                // and create a ValidationError with the same field and message
                                fieldErrors.forEach(validationError ->
                                        errors.add(new ValidationError(
                                                field.getKey(),  // Using the field key as the field name
                                                validationError.toString()  // Convert the error to string as the message
                                        ))
                                );
                            }
                        } catch (IllegalArgumentException e) {
                            errors.add(new ValidationError(
                                    field.getKey(),
                                    String.format("Unsupported field type: %s", field.getFieldType())
                            ));
                        }
                    });
        });

        return errors;
    }


    private Application findApplicationById(
            UUID applicationId

    ) {
        return  applicationRepository.findById(applicationId)
                .orElseThrow(() -> ApiException.notFound("Application not found with id "+ applicationId));
    }

    public AIResultDTO getApplicationAIResult(UUID applicationId) {
        return  aiClient.getAIResultByApplicationId(String.valueOf(applicationId));
    }

    private FarmerReponse getFarmerInfo(UUID farmerId, UUID userId) {
        return farmerClient.getFarmerById(farmerId, userId);
    }

    private String getPresignedUrl(UUID documentId, UUID userId) {
        return documentServiceClient.generatePresignedUrl(userId,documentId,60);
    }
}
