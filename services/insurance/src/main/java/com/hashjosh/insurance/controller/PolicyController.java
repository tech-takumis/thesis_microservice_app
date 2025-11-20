package com.hashjosh.insurance.controller;

import com.hashjosh.constant.policy.CreatePolicyRequest;
import com.hashjosh.constant.policy.PolicyResponse;
import com.hashjosh.constant.policy.UpdatePolicyRequest;
import com.hashjosh.insurance.service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping
    public ResponseEntity<PolicyResponse> createPolicy(@Valid @RequestBody CreatePolicyRequest request) {
        PolicyResponse response = policyService.createPolicy(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyResponse> getPolicyById(@PathVariable UUID id) {
        PolicyResponse response = policyService.getPolicyById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/insurance/{insuranceId}")
    public ResponseEntity<PolicyResponse> getPolicyByInsuranceId(@PathVariable UUID insuranceId) {
        PolicyResponse response = policyService.getPolicyByInsuranceId(insuranceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/policy-number/{policyNumber}")
    public ResponseEntity<PolicyResponse> getPolicyByPolicyNumber(@PathVariable String policyNumber) {
        PolicyResponse response = policyService.getPolicyByPolicyNumber(policyNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PolicyResponse>> getAllPolicies() {
        List<PolicyResponse> responses = policyService.getAllPolicies();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<PolicyResponse>> getPoliciesByFarmerId(@PathVariable UUID farmerId) {
        List<PolicyResponse> responses = policyService.getPoliciesByFarmerId(farmerId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/active")
    public ResponseEntity<List<PolicyResponse>> getActivePolicies() {
        List<PolicyResponse> responses = policyService.getActivePolicies();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/expired")
    public ResponseEntity<List<PolicyResponse>> getExpiredPolicies() {
        List<PolicyResponse> responses = policyService.getExpiredPolicies();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PolicyResponse> updatePolicy(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePolicyRequest request) {
        PolicyResponse response = policyService.updatePolicy(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(@PathVariable UUID id) {
        policyService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }
}
