package com.hashjosh.application.service;

import com.hashjosh.application.dto.fields.ApplicationFieldsRequestDto;
import com.hashjosh.application.dto.sections.ApplicationSectionRequestDto;
import com.hashjosh.application.dto.type.ApplicationTypeRequestDto;
import com.hashjosh.constant.application.ApplicationTypeResponseDto;
import com.hashjosh.application.exceptions.ApiException;
import com.hashjosh.application.mapper.ApplicationTypeMapper;
import com.hashjosh.application.model.*;
import com.hashjosh.application.repository.ApplicationProviderRepository;
import com.hashjosh.application.repository.ApplicationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationTypeService {

    private final ApplicationTypeRepository applicationTypeRepository;
    private final ApplicationSectionService applicationSectionService;
    private final ApplicationFieldsService applicationFieldService;
    private final ApplicationTypeMapper applicationTypeMapper;
    private final ApplicationProviderRepository applicationProviderRepository;


    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    public ApplicationTypeResponseDto create(ApplicationTypeRequestDto dto) {
        ApplicationProvider provider = applicationProviderRepository
                .findByName(dto.providerName())
                .orElseThrow(() -> ApiException.badRequest("Application provider not found"));

        ApplicationType applicationType = applicationTypeMapper.toApplicationType(dto);
        applicationType.setProvider(provider);
        ApplicationType savedApplicationType = applicationTypeRepository.save(applicationType);
        boolean requiredAIAnalysis = false;

        List<ApplicationSection> applicationSections = new ArrayList<>();
        for (ApplicationSectionRequestDto sectionRequestDto : dto.sections()) {
            ApplicationSection section = applicationSectionService.create(sectionRequestDto, savedApplicationType);

            List<ApplicationField> applicationFields = new ArrayList<>();
            for (ApplicationFieldsRequestDto field : sectionRequestDto.fields()) {
                ApplicationField savedField = applicationFieldService.create(field, section.getId());
                applicationFields.add(savedField);
            }

            if(applicationFields.stream().anyMatch(field -> Boolean.TRUE.equals(field.getRequiredAIAnalysis()))){
                requiredAIAnalysis = true;
            }

            section.setFields(applicationFields);
            applicationSections.add(section);
        }

        applicationType.setRequiredAIAnalysis(requiredAIAnalysis);
        applicationType.setSections(applicationSections);
        return applicationTypeMapper.toApplicationResponse(applicationType,false,false);
    }

    public List<ApplicationTypeResponseDto> findAll(Boolean includeApplicationResponse,
                                                    Boolean includeSections) {
        return applicationTypeRepository.findAll().stream()
                .map(application -> applicationTypeMapper.toApplicationResponse(application,includeApplicationResponse,includeSections))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicationTypeResponseDto> findByProvider(
            String provider,
            Boolean includeApplicationResponse,
            Boolean includeSections
    ) {
        return applicationTypeRepository.findAllByProvider_Name(provider)
                .stream().map(application -> applicationTypeMapper.toApplicationResponse(application,includeApplicationResponse,includeSections))
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationTypeResponseDto findById(
            UUID id,
            Boolean includeApplicationResponse,
            Boolean includeSections
    ) {
        ApplicationType applicationType = applicationTypeRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Application type not found"));
        return applicationTypeMapper.toApplicationResponse(applicationType,includeApplicationResponse,includeSections);
    }

    @Transactional(readOnly = true)
    public void deleteById(UUID id) {
        ApplicationType applicationType = applicationTypeRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Application type not found"));
        applicationTypeRepository.delete(applicationType);
    }

    public Boolean requiresPredictions(UUID applicationTypeId) {
        ApplicationType applicationType = applicationTypeRepository.findById(applicationTypeId)
                .orElseThrow(() -> ApiException.notFound("Application type not found"));
        if (applicationType.getSections() == null) return false;
        for (ApplicationSection section : applicationType.getSections()) {
            if (section.getFields() == null) continue;
            for (ApplicationField field : section.getFields()) {
                if (Boolean.TRUE.equals(field.getRequiredAIAnalysis())) {
                    return true;
                }
            }
        }
        return false;
    }
}
