package com.hashjosh.insurance.controller;

import com.hashjosh.insurance.dto.batch.*;
import com.hashjosh.insurance.service.BatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
@Slf4j
public class BatchController {

    private final BatchService batchService;


    @PostMapping
    public ResponseEntity<String> createBatch(
            @RequestBody @Valid BatchRequestDTO request
    ) {
        log.info("Creating a new batch");
        BatchResponseDTO response = batchService.createBatch(request);
        return ResponseEntity.ok("Batch created with ID: " + response.getId());
    }

    @GetMapping
    public ResponseEntity<List<BatchResponseDTO>> getAllBatches(
    ) {
        log.info("Fetching all batches");
        List<BatchResponseDTO> batches = batchService.getAllBatches();
        return ResponseEntity.ok(batches);
    }


    @GetMapping("/{batch_id}")
    public ResponseEntity<BatchResponseDTO> getBatchById(
            @PathVariable("batch_id") UUID batchId
    ) {
        log.info("Fetching batch with ID: {}", batchId);
        BatchResponseDTO batch = batchService.getBatchById(batchId);
        return ResponseEntity.ok(batch);
    }

    @GetMapping("/application-type/{applicationTypeId}")
    public ResponseEntity<List<BatchResponseDTO>> getBatchByApplicationTypeId(
            @PathVariable("applicationTypeId") UUID applicationTypeId
    ){
        return ResponseEntity.ok(batchService.findBacthesByApplicationTypeId(applicationTypeId));
    }

}
