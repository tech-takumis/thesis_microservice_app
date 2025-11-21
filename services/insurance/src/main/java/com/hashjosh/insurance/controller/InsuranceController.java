package com.hashjosh.insurance.controller;

import com.hashjosh.insurance.dto.insurance.InsuranceRequestDTO;
import com.hashjosh.insurance.dto.insurance.InsuranceResponse;
import com.hashjosh.insurance.dto.insurance.InsuranceStatusStatisticDTO;
import com.hashjosh.insurance.service.InsuranceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/insurance")
@RequiredArgsConstructor
public class InsuranceController {

    private final InsuranceService insuranceService;

    @GetMapping
    public ResponseEntity<List<InsuranceResponse>> getAllInsurance(
    ){
        return ResponseEntity.ok(insuranceService.findAll());
    }

    @GetMapping("/verified")
    public ResponseEntity<List<InsuranceResponse>> getAllVerifiedInsurance(){
        return ResponseEntity.ok(insuranceService.findAllVerified());
    }

    @GetMapping("/user/all")
    public  ResponseEntity<List<InsuranceResponse>> getInsuranceByUser(){
        return ResponseEntity.ok(insuranceService.findByCurrentUser());
    }

    @GetMapping("/{insurance-Id}")
    public ResponseEntity<InsuranceResponse> getInsuranceById(
                @PathVariable("insurance-Id") UUID insuranceId
    ){
        return ResponseEntity.ok(insuranceService.findById(insuranceId));
    }

    @GetMapping("/application/{application-id}")
    public ResponseEntity<InsuranceResponse> getInsuranceByApplicationId(
            @PathVariable("application-id") UUID applicationId
    ){
        return ResponseEntity.ok(insuranceService.findByApplicationId(applicationId));
    }

    @GetMapping("/application-type/{application-type}")
    public ResponseEntity<List<InsuranceResponse>> getInsuranceByApplicationTypeId(
            @PathVariable("application-type") UUID applicationTypeId
    ){
        return ResponseEntity.ok(insuranceService.findByApplicationTypeId(applicationTypeId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<InsuranceResponse>> getInsuranceByStatus(@PathVariable("status") com.hashjosh.constant.pcic.enums.InsuranceStatus status) {
        return ResponseEntity.ok(insuranceService.findByStatus(status));
    }

    @GetMapping("/statistics/status")
    public ResponseEntity<List<InsuranceStatusStatisticDTO>> getInsuranceStatisticsByStatus() {
        return ResponseEntity.ok(insuranceService.getInsuranceStatisticsByStatus());
    }

    @PutMapping("/{insurance-Id}")
    public ResponseEntity<InsuranceResponse> updateInsurance(
            @PathVariable("insurance-Id")UUID insuranceId,
            @RequestBody @Valid InsuranceRequestDTO request
            ){
        return ResponseEntity.ok(insuranceService.update(insuranceId,request));
    }

    @DeleteMapping("/{insurance-Id}")
    public ResponseEntity<Void> deleteInsurance(
            @PathVariable("insurance-Id") UUID insuranceId
    ){
        insuranceService.delete(insuranceId);
        return ResponseEntity.ok().build();
    }
}
