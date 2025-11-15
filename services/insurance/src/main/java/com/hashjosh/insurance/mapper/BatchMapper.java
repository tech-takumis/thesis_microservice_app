package com.hashjosh.insurance.mapper;

import com.hashjosh.constant.application.ApplicationResponseDto;
import com.hashjosh.constant.farmer.FarmerReponse;
import com.hashjosh.insurance.clients.ApplicationClient;
import com.hashjosh.insurance.clients.FarmerClient;
import com.hashjosh.insurance.dto.batch.BatchRequestDTO;
import com.hashjosh.insurance.dto.batch.BatchResponseDTO;
import com.hashjosh.insurance.dto.insurance.InsuranceResponse;
import com.hashjosh.insurance.entity.Batch;
import com.hashjosh.insurance.entity.Insurance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchMapper {

    private final FarmerClient farmerClient;
    private final ApplicationClient applicationClient;


    public Batch toEntity(BatchRequestDTO request) {
        return Batch.builder()
                .name(request.getName())
                .applicationTypeId(request.getApplicationTypeId())
                .description(request.getDescription())
                .isAvailable(request.isAvailable())
                .maxApplications(request.getMaxApplications())
                .endDate(request.getEndDate())
                .startDate(request.getStartDate())
                .build();
    }

    public BatchResponseDTO toBatchResponseDTO(Batch save, Boolean includeInsurances) {
        List<InsuranceResponse> insurances = new ArrayList<>();

        BatchResponseDTO builder = BatchResponseDTO.builder()
                .id(save.getId())
                .batchName(save.getName())
                .description(save.getDescription())
                .maxApplications(save.getMaxApplications())
                .totalApplications(save.getTotalApplications())
                .isAvailable(save.isAvailable())
                .endDate(save.getEndDate())
                .startDate(save.getStartDate())
                .build();

        if(includeInsurances != null && includeInsurances && save.getInsurances() != null){
            save.getInsurances().forEach(insurance -> {
                 insurances.add(mapInsuranceToBasicResponse(insurance));
             });
             builder.setInsurances(insurances);
        }

        return builder;
    }
    // Enhanced mapping to include farmer and application data
    private InsuranceResponse mapInsuranceToBasicResponse(Insurance insurance) {
        InsuranceResponse.InsuranceResponseBuilder builder = InsuranceResponse.builder()
                .insuranceId(insurance.getId())
                .requiredAIProcessing(insurance.getIsAIProcessed())
                .createdAt(insurance.getCreatedAt())
                .updatedAt(insurance.getUpdatedAt());


        // Fetch and include application data
        if (insurance.getSubmissionId() != null && insurance.getFarmerId() != null) {
            try {
                ApplicationResponseDto application = applicationClient.getApplicationById(
                    insurance.getSubmissionId(),
                    insurance.getFarmerId().toString()
                );
                builder.application(application);
            } catch (Exception e) {
                log.warn("Failed to fetch application data for submission ID {}: {}",
                        insurance.getSubmissionId(), e.getMessage());
            }
        }

        return builder.build();
    }

}
