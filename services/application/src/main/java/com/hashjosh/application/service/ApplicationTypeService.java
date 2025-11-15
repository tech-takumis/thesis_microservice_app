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
    public ApplicationTypeResponseDto create(ApplicationTypeRequestDto dto, Boolean sections, Boolean fields) {
        ApplicationProvider provider = applicationProviderRepository
                .findByName(dto.providerName())
                .orElseThrow(() -> ApiException.badRequest("Application provider not found"));

        ApplicationType applicationType = applicationTypeMapper.toApplicationType(dto);
        applicationType.setProvider(provider);
        ApplicationType savedApplicationType = applicationTypeRepository.save(applicationType);

        List<ApplicationSection> applicationSections = new ArrayList<>();
        for (ApplicationSectionRequestDto sectionRequestDto : dto.sections()) {
            ApplicationSection section = applicationSectionService.create(sectionRequestDto, savedApplicationType);

            List<ApplicationField> applicationFields = new ArrayList<>();
            for (ApplicationFieldsRequestDto field : sectionRequestDto.fields()) {
                ApplicationField savedField = applicationFieldService.create(field, section.getId());
                applicationFields.add(savedField);
            }

            section.setFields(applicationFields);
            applicationSections.add(section);
        }

        applicationType.setSections(applicationSections);
        return applicationTypeMapper.toApplicationResponse(applicationType, sections, fields);
    }

    public List<ApplicationTypeResponseDto> findAll(Boolean sections, Boolean fields) {
        return applicationTypeRepository.findAll().stream()
                .map(applicationType -> applicationTypeMapper
                        .toApplicationResponse(applicationType, sections, fields))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicationTypeResponseDto> findAllWithFilter(
            String provider,
            Boolean sections,
            Boolean fields
    ) {
        return applicationTypeRepository.findAllByProvider_Name(provider)
                .stream().map(appType -> applicationTypeMapper.toApplicationResponse(appType, sections, fields))
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationTypeResponseDto findById(
            UUID id,
            Boolean sections,
            Boolean fields
    ) {
        ApplicationType applicationType = applicationTypeRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Application type not found"));
        return applicationTypeMapper.toApplicationResponse(applicationType, sections, fields);
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
