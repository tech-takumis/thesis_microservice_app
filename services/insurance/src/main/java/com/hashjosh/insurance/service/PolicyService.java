package com.hashjosh.insurance.service;

import com.hashjosh.constant.pcic.enums.InsuranceStatus;
import com.hashjosh.constant.policy.CreatePolicyRequest;
import com.hashjosh.constant.policy.PolicyResponse;
import com.hashjosh.constant.policy.UpdatePolicyRequest;
import com.hashjosh.insurance.entity.Insurance;
import com.hashjosh.insurance.entity.Policy;
import com.hashjosh.insurance.exception.ApiException;
import com.hashjosh.insurance.kafka.KafkaProducer;
import com.hashjosh.insurance.mapper.PolicyMapper;
import com.hashjosh.insurance.repository.InsuranceRepository;
import com.hashjosh.insurance.repository.PolicyRepository;
import com.hashjosh.kafkacommon.application.PolicyIssuedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyService {
    private final PolicyRepository policyRepository;
    private final PolicyMapper policyMapper;
    private final InsuranceRepository insuranceRepository;
    private final KafkaProducer kafkaProducer;

    @Transactional
    public PolicyResponse createPolicy(CreatePolicyRequest request) {
        // Check if insurance exists
        Insurance insurance = insuranceRepository.findById(request.getInsuranceId())
                .orElseThrow(() -> ApiException.notFound("Insurance not found with id: " + request.getInsuranceId()));

        // Check if insurance already has a policy
        if (policyRepository.findByInsuranceId(request.getInsuranceId()).isPresent()) {
            throw ApiException.conflict("Insurance already has a policy");
        }

        // Generate unique policy number
        String policyNumber = generatePolicyNumber(insurance);

        Policy policy = policyMapper.toEntity(request, insurance);
        policy.setPolicyNumber(policyNumber);
        Policy savedPolicy = policyRepository.save(policy);

        insurance.setCurrentStatus(InsuranceStatus.POLICY_ISSUED);
        insuranceRepository.save(insurance);

        PolicyIssuedEvent event = PolicyIssuedEvent.builder()
                .submissionId(insurance.getSubmissionId())
                .userId(insurance.getFarmerId())
                .policyId(savedPolicy.getId())
                .policyNumber(savedPolicy.getPolicyNumber())
                .issuedAt(LocalDateTime.now())
                .build();

        kafkaProducer.publishEvent("application-policy-issued", event);

        return policyMapper.toResponse(savedPolicy);
    }

    private String generatePolicyNumber(Insurance insurance) {
        // Format: POL-YYYY-XXXXXXXX (where XXXXXXXX is first 8 chars of UUID)
        String year = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy"));
        String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String policyNumber = String.format("POL-%s-%s", year, uniqueId);

        // Ensure uniqueness (very rare collision but just in case)
        while (policyRepository.findByPolicyNumber(policyNumber).isPresent()) {
            uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            policyNumber = String.format("POL-%s-%s", year, uniqueId);
        }

        return policyNumber;
    }

    @Transactional(readOnly = true)
    public PolicyResponse getPolicyById(UUID id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Policy not found with id: " + id));

        return policyMapper.toResponse(policy);
    }

    @Transactional(readOnly = true)
    public PolicyResponse getPolicyByInsuranceId(UUID insuranceId) {
        Policy policy = policyRepository.findByInsuranceId(insuranceId)
                .orElseThrow(() -> ApiException.notFound("Policy not found for insurance id: " + insuranceId));

        return policyMapper.toResponse(policy);
    }

    @Transactional(readOnly = true)
    public PolicyResponse getPolicyByPolicyNumber(String policyNumber) {
        Policy policy = policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> ApiException.notFound("Policy not found with policy number: " + policyNumber));

        return policyMapper.toResponse(policy);
    }

    @Transactional(readOnly = true)
    public List<PolicyResponse> getAllPolicies() {
        return policyRepository.findAll()
                .stream()
                .map(policyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PolicyResponse> getPoliciesByFarmerId(UUID farmerId) {
        return policyRepository.findByFarmerId(farmerId)
                .stream()
                .map(policyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PolicyResponse> getActivePolicies() {
        return policyRepository.findActivePolicies()
                .stream()
                .map(policyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PolicyResponse> getExpiredPolicies() {
        return policyRepository.findExpiredPolicies()
                .stream()
                .map(policyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PolicyResponse updatePolicy(UUID id, UpdatePolicyRequest request) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Policy not found with id: " + id));

        // Check if policy number is being updated and if it already exists
        if (request.getPolicyNumber() != null &&
            !request.getPolicyNumber().equals(policy.getPolicyNumber())) {
            if (policyRepository.findByPolicyNumber(request.getPolicyNumber()).isPresent()) {
                throw ApiException.conflict("Policy with policy number " + request.getPolicyNumber() + " already exists");
            }
        }

        policyMapper.updateEntity(policy, request);
        Policy updatedPolicy = policyRepository.save(policy);

        return policyMapper.toResponse(updatedPolicy);
    }

    @Transactional
    public void deletePolicy(UUID id) {
        if (!policyRepository.existsById(id)) {
            throw ApiException.notFound("Policy not found with id: " + id);
        }

        policyRepository.deleteById(id);
    }
}
