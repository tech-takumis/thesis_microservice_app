package com.hashjosh.insurance.service;

import com.hashjosh.insurance.dto.batch.BatchRequestDTO;
import com.hashjosh.insurance.dto.batch.BatchResponseDTO;
import com.hashjosh.insurance.entity.Batch;
import com.hashjosh.insurance.exception.ApiException;
import com.hashjosh.insurance.mapper.BatchMapper;
import com.hashjosh.insurance.repository.BatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchService {

    private final BatchRepository batchRepository;
    private final BatchMapper batchMapper;

    public BatchResponseDTO createBatch(
            BatchRequestDTO request
    ) {
        log.info("Creating batch in the service layer");
        Batch batch = batchMapper.toEntity(request);

        return batchMapper.toBatchResponseDTO(batchRepository.save(batch),true);
    }

    public List<BatchResponseDTO> getAllBatches(
            Boolean includeInsurances
    ) {
        log.info("Retrieving all batches in the service layer");
        List<Batch> batches = batchRepository.findAll();
        return batches.stream()
                .map(batch -> {
                    return batchMapper.toBatchResponseDTO(batch,includeInsurances);
                })
                .toList();
    }

    public BatchResponseDTO getBatchById(UUID batchId,Boolean includeInsurances) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> ApiException.notFound("Batch not found"));
        return batchMapper.toBatchResponseDTO(batch,includeInsurances);
    }

    public List<BatchResponseDTO> findBacthesByApplicationTypeId(UUID applicationTypeId,Boolean includeInsurances)
    {
            return batchRepository.findAllAvailableBatchesByApplicationTypeId(applicationTypeId)
                    .stream().map(batch -> {
                        return batchMapper.toBatchResponseDTO(batch,includeInsurances);
                    })
                    .toList();

    }
}
