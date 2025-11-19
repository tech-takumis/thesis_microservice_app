package com.hashjosh.application.runner;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.hashjosh.application.repository.ApplicationWorkflowRepository;
import com.hashjosh.constant.application.FieldType;
import com.hashjosh.application.model.*;
import com.hashjosh.application.repository.ApplicationProviderRepository;
import com.hashjosh.application.repository.ApplicationTypeRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationTypeInitializer implements CommandLineRunner {

    private final EntityManager entityManager;
    private final ApplicationTypeRepository applicationTypeRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationProviderRepository applicationProviderRepository;
    private final ApplicationWorkflowRepository workflowRepository;

    public boolean isApplicationNotNull(){
        return applicationTypeRepository.count() > 0;
    }
    @Override
    @Transactional
    public void run(String... args) {
        if(isApplicationNotNull()){
            log.info("Application Type already exists initialization skipped!");
            return;
        }

        ApplicationProvider provider1 = ApplicationProvider.builder()
                .name("Agriculture")
                .description("Agriculture related application forms")
                .createdAt(LocalDateTime.now())
                .build();

        applicationProviderRepository.save(provider1);
        ApplicationProvider provider2 = ApplicationProvider.builder()
                .name("PCIC")
                .description("Pcic related application forms")
                .createdAt(LocalDateTime.now())
                .build();

        applicationProviderRepository.save(provider2);

        ApplicationWorkflow cropInsuranceWorkflow = ApplicationWorkflow.builder()
                .verificationEnabled(true)
                .inspectionEnabled(false)
                .policyEnabled(true)
                .claimEnabled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ApplicationWorkflow savedCropWorkflow = workflowRepository.save(cropInsuranceWorkflow);

        // Application Type 1: Crop Insurance Application
        ApplicationType cropInsurance = ApplicationType.builder()
                .name("Crop Insurance Application")
                .description("Application form for insuring rice or corn crops")
                .layout("form")
                .printable(true)
                .provider(provider1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .applicationWorkflow(savedCropWorkflow)
                .sections(new ArrayList<>())
                .build();

        // Sections for Crop Insurance
        List<ApplicationSection> cropInsuranceSections = new ArrayList<>();



        // Section I: Basic Information
        ApplicationSection basicInfoSection = ApplicationSection.builder()
                .title("Basic Information")
                .applicationType(cropInsurance)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .fields(new ArrayList<>())
                .build();
        cropInsuranceSections.add(basicInfoSection);

        // Fields for Basic Information
        List<ApplicationField> basicInfoFields = new ArrayList<>();
        basicInfoFields.add(createField("last_name", "Last Name", FieldType.TEXT, true, false,null, basicInfoSection));
        basicInfoFields.add(createField("first_name", "First Name", FieldType.TEXT, true,false, null, basicInfoSection));
        basicInfoFields.add(createField("middle_name", "Middle Name", FieldType.TEXT, false, false,null, basicInfoSection));
        basicInfoFields.add(createField("address", "Address", FieldType.TEXT, true, false,null, basicInfoSection));
        basicInfoFields.add(createField("cell_phone_number", "Cell Phone Number", FieldType.TEXT, false,false, null, basicInfoSection));
        basicInfoFields.add(createField("sex", "Sex", FieldType.SELECT, true,false, createChoices(new String[]{"Male", "Female"}), basicInfoSection));
        basicInfoFields.add(createField("age", "Age", FieldType.NUMBER, false,false, null, basicInfoSection));
        basicInfoFields.add(createField("date_of_birth", "Date of Birth", FieldType.DATE, false, false,null, basicInfoSection));
        basicInfoFields.add(createField("indigenous_people", "Indigenous People", FieldType.BOOLEAN, false,false, null, basicInfoSection));
        basicInfoFields.add(createField("tribe", "Tribe", FieldType.TEXT, false, false,null, basicInfoSection));
        basicInfoFields.add(createField("civil_status", "Civil Status", FieldType.SELECT, true,false, createChoices(new String[]{"Single", "Married", "Widow/Widower", "Separated"}), basicInfoSection));
        basicInfoFields.add(createField("spouse_name", "Name of Spouse", FieldType.TEXT, false,false, null, basicInfoSection));
        basicInfoFields.add(createField("primary_beneficiary", "Primary Beneficiary", FieldType.TEXT, false, false,null, basicInfoSection));
        basicInfoFields.add(createField("secondary_beneficiary", "Secondary Beneficiary", FieldType.TEXT, false, false,null, basicInfoSection));
        basicInfoSection.setFields(basicInfoFields);

        // Section B: The Farm
        ApplicationSection farmSection = ApplicationSection.builder()
                .title("The Farm")
                .applicationType(cropInsurance)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .fields(new ArrayList<>())
                .build();
        cropInsuranceSections.add(farmSection);

        // Fields for The Farm (Lot 1, extensible for Lot 2, Lot 3)
        List<ApplicationField> farmFields = new ArrayList<>();
        farmFields.add(createField("lot_1_area", "Lot 1 Area (ha)", FieldType.NUMBER, true, false,null, farmSection));
        farmFields.add(createField("lot_1_location", "Lot 1 Location", FieldType.JSON, true, false,null, farmSection)); // Sitio, Barangay, Municipality, Province
        farmFields.add(createField("lot_1_boundaries", "Lot 1 Boundaries", FieldType.JSON, false, false,null, farmSection)); // North, South, East, West
        farmFields.add(createField("lot_1_variety", "Lot 1 Variety", FieldType.TEXT, true, false,null, farmSection));
        farmFields.add(createField("lot_1_planting_method", "Lot 1 Planting Method", FieldType.SELECT, true,false, createChoices(new String[]{"Direct Seeding", "Transplanting"}), farmSection));
        farmFields.add(createField("lot_1_date_sowing", "Lot 1 Date of Sowing", FieldType.DATE, false,false, null, farmSection));
        farmFields.add(createField("lot_1_date_planting", "Lot 1 Date of Planting", FieldType.DATE, true,false, null, farmSection));
        farmFields.add(createField("lot_1_date_harvest", "Lot 1 Date of Harvest", FieldType.DATE, true,false, null, farmSection));
        farmFields.add(createField("lot_1_tenurial_status", "Lot 1 Tenurial Status", FieldType.SELECT, true,false, createChoices(new String[]{"Owner", "Lessee"}), farmSection));
        // Add similar fields for Lot 2, Lot 3 if needed
        farmSection.setFields(farmFields);


        // Section II: Certification
        ApplicationSection certificationSection = ApplicationSection.builder()
                .title("Certification")
                .applicationType(cropInsurance)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .fields(new ArrayList<>())
                .build();
        cropInsuranceSections.add(certificationSection);

        // Fields for Certification
        List<ApplicationField> certificationFields = new ArrayList<>();
        certificationFields.add(createField("farmer_signature", "Farmer Signature/Thumb Mark", FieldType.SIGNATURE, true, false,null, certificationSection));
        certificationSection.setFields(certificationFields);

        cropInsurance.setSections(cropInsuranceSections);

        entityManager.persist(cropInsurance);

        ApplicationWorkflow claimInsuranceWorkflow = ApplicationWorkflow.builder()
                .verificationEnabled(true)
                .inspectionEnabled(true)
                .policyEnabled(false)
                .claimEnabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        ApplicationWorkflow savedClaimWorkflow = workflowRepository.save(claimInsuranceWorkflow);

        // Application Type 2: Claim for Indemnity
        ApplicationType claimIndemnity = ApplicationType.builder()
                .name("Claim for Indemnity")
                .description("Claim form for indemnity of insured high-value crops")
                .layout("form")
                .printable(false)
                .provider(provider2)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .applicationWorkflow(savedClaimWorkflow)
                .sections(new ArrayList<>())
                .build();

        // Sections for Claim for Indemnity
        List<ApplicationSection> claimIndemnitySections = new ArrayList<>();

        // Section I: Basic Information
        ApplicationSection claimBasicInfoSection = ApplicationSection.builder()
                .title("Basic Information")
                .applicationType(claimIndemnity)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .fields(new ArrayList<>())
                .build();
        claimIndemnitySections.add(claimBasicInfoSection);

        // Fields for Basic Information
        List<ApplicationField> claimBasicInfoFields = new ArrayList<>();
        claimBasicInfoFields.add(createField("farmer_name", "Name of Farmer-Assured", FieldType.TEXT, true, false,null, claimBasicInfoSection));
        claimBasicInfoFields.add(createField("address", "Address", FieldType.TEXT, true, false,null, claimBasicInfoSection));
        claimBasicInfoFields.add(createField("cell_phone_number", "Cell Phone Number", FieldType.TEXT, false, false,null, claimBasicInfoSection));
        claimBasicInfoFields.add(createField("farm_location", "Location of Farm", FieldType.TEXT, true, false,null, claimBasicInfoSection));
        claimBasicInfoFields.add(createField("insured_crops", "Insured Crops", FieldType.SELECT, true, false,createChoices(new String[]{"Rice", "Corn", "High Value Crops"}), claimBasicInfoSection));
        claimBasicInfoFields.add(createField("area_insured", "Area Insured (in hectares)", FieldType.NUMBER, true, false,null, claimBasicInfoSection));
        claimBasicInfoFields.add(createField("variety_planted", "Variety Planted", FieldType.SELECT, true, false,createChoices(new String[]{"Inbred", "Inbred (Seed Production)", "Hybrid", "Hybrid (Seed Production)"}), claimBasicInfoSection));
        claimBasicInfoFields.add(createField("date_planting", "Actual Date of Planting", FieldType.DATE, true, false,null, claimBasicInfoSection));
        claimBasicInfoFields.add(createField("cic_no", "CIC Number", FieldType.TEXT, true, false,null, claimBasicInfoSection));
        // Additional fields for PCIC claim computation
        claimBasicInfoFields.add(createField("planting_method", "Planting Method", FieldType.SELECT, false, false,createChoices(new String[]{"Direct Seeding", "Transplanting"}), claimBasicInfoSection));
        claimBasicInfoFields.add(createField("tenurial_status", "Tenurial Status", FieldType.SELECT, false, false,createChoices(new String[]{"Owner", "Lessee", "Tenant"}), claimBasicInfoSection));
        claimBasicInfoSection.setFields(claimBasicInfoFields);

        // Section II: Damage Indicators
        ApplicationSection damageIndicatorsSection = ApplicationSection.builder()
                .title("Damage Indicators")
                .applicationType(claimIndemnity)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .fields(new ArrayList<>())
                .build();
        claimIndemnitySections.add(damageIndicatorsSection);

        // Fields for Damage Indicators
        List<ApplicationField> damageIndicatorsFields = new ArrayList<>();
        damageIndicatorsFields.add(createField("cause_of_loss_leaf", "Take a picture to the specific damage (e.g Leaf)", FieldType.FILE, true, true,null, damageIndicatorsSection));
        damageIndicatorsFields.add(createField("cause_of_loss_overall", "Take a picture to whole damage", FieldType.FILE, true, false,null, damageIndicatorsSection));
        damageIndicatorsFields.add(createField("cause_of_loss_with_self", "Take a picture with the damage including yourself", FieldType.FILE, true, false,null, damageIndicatorsSection));
        damageIndicatorsFields.add(createField("date_of_loss", "Date of Loss Occurrence", FieldType.DATE, true, false,null, damageIndicatorsSection));
        damageIndicatorsFields.add(createField("cultivation_stage", "Age/Stage of Cultivation at Time of Loss", FieldType.SELECT, true, false,createChoices(new String[]{"1 month", "2 months", "3 months", "4 months", "5 months", "Seedling", "Vegetative", "Reproductive", "Maturity", "Near Harvest"}), damageIndicatorsSection));
        damageIndicatorsFields.add(createField("area_damaged", "Area Damaged (in hectares)", FieldType.NUMBER, true, false,null, damageIndicatorsSection));
        damageIndicatorsFields.add(createField("expected_harvest_date", "Expected Date of Harvest", FieldType.DATE, true, false,null, damageIndicatorsSection));
        // Additional fields for better claim assessment
        damageIndicatorsFields.add(createField("cause_of_damage", "Primary Cause of Damage", FieldType.SELECT, true, false,createChoices(new String[]{"Drought", "Flood", "Typhoon", "Pest Infestation", "Disease", "Hail", "Fire", "Other Natural Calamity"}), damageIndicatorsSection));
        damageIndicatorsFields.add(createField("damage_description", "Description of Damage", FieldType.TEXT, false, false,null, damageIndicatorsSection));
        damageIndicatorsFields.add(createField("percentage_damage", "Estimated Percentage of Damage (%)", FieldType.NUMBER, false, false,null, damageIndicatorsSection));
        damageIndicatorsSection.setFields(damageIndicatorsFields);

        // Section III: Location Sketch Plan
        ApplicationSection locationSketchSection = ApplicationSection.builder()
                .title("Location Sketch Plan of Damaged Insured Crops")
                .applicationType(claimIndemnity)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .fields(new ArrayList<>())
                .build();
        claimIndemnitySections.add(locationSketchSection);

        // Fields for Location Sketch Plan
        List<ApplicationField> locationSketchFields = new ArrayList<>();
        locationSketchFields.add(createField("lot_1_area", "Lot 1 Area (ha)", FieldType.NUMBER, false, false,null, locationSketchSection));
        locationSketchFields.add(createField("lot_1_boundaries", "Lot 1 Boundaries", FieldType.JSON, false, false,null, locationSketchSection)); // North, South, East, West

        locationSketchFields.add(createField("lot_2_area", "Lot 2 Area (ha)", FieldType.NUMBER, false, false,null, locationSketchSection));
        locationSketchFields.add(createField("lot_2_boundaries", "Lot 2 Boundaries", FieldType.JSON, false, false,null, locationSketchSection)); // North, South, East, West

        locationSketchFields.add(createField("lot_3_area", "Lot 3 Area (ha)", FieldType.NUMBER, false, false,null, locationSketchSection));
        locationSketchFields.add(createField("lot_3_boundaries", "Lot 3 Boundaries", FieldType.JSON, false, false,null, locationSketchSection)); // North, South, East, West

        locationSketchFields.add(createField("lot_4_area", "Lot 4 Area (ha)", FieldType.NUMBER, false, false,null, locationSketchSection));
        locationSketchFields.add(createField("lot_4_boundaries", "Lot 4 Boundaries", FieldType.JSON, false, false,null, locationSketchSection)); // North, South, East, West
        // Add similar fields for Lot 2, Lot 3, Lot 4 if needed
        locationSketchSection.setFields(locationSketchFields);


        claimIndemnity.setSections(claimIndemnitySections);
        entityManager.persist(claimIndemnity);
    }

    private ApplicationField createField(String key, String fieldName, FieldType fieldType, boolean required,boolean requiredAIAnalysis, JsonNode choices, ApplicationSection section) {
        return ApplicationField.builder()
                .key(key)
                .fieldName(fieldName)
                .fieldType(fieldType)
                .required(required)
                .choices(choices)
                .applicationSection(section)
                .requiredAIAnalysis(requiredAIAnalysis)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private JsonNode createChoices(String[] options) {
        ArrayNode choices = objectMapper.createArrayNode();
        for (String option : options) {
            choices.add(option);
        }
        return choices;
    }
}
