package com.hashjosh.insurance.service;

import com.hashjosh.constant.policy.CreatePolicyRequest;
import com.hashjosh.constant.policy.PolicyResponse;
import com.hashjosh.insurance.entity.Insurance;
import com.hashjosh.insurance.entity.Policy;
import com.hashjosh.insurance.exception.ApiException;
import com.hashjosh.insurance.mapper.PolicyMapper;
import com.hashjosh.insurance.repository.InsuranceRepository;
import com.hashjosh.insurance.repository.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyMapper policyMapper;

    @Mock
    private InsuranceRepository insuranceRepository;

    @InjectMocks
    private PolicyService policyService;

    @Test
    void createPolicy_Success() {
        // Arrange
        UUID insuranceId = UUID.randomUUID();
        CreatePolicyRequest request = CreatePolicyRequest.builder()
                .insuranceId(insuranceId)
                .effectiveDate(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusYears(1))
                .build();

        Insurance insurance = new Insurance();
        insurance.setId(insuranceId);

        Policy policy = new Policy();
        policy.setId(UUID.randomUUID());
        policy.setPolicyNumber("POL-2025-ABC12345");

        PolicyResponse expectedResponse = PolicyResponse.builder()
                .id(policy.getId())
                .insuranceId(insuranceId)
                .policyNumber("POL-2025-ABC12345")
                .build();

        when(insuranceRepository.findById(insuranceId)).thenReturn(Optional.of(insurance));
        when(policyRepository.findByInsuranceId(insuranceId)).thenReturn(Optional.empty());
        when(policyRepository.findByPolicyNumber(any())).thenReturn(Optional.empty());
        when(policyMapper.toEntity(request, insurance)).thenReturn(policy);
        when(policyRepository.save(policy)).thenReturn(policy);
        when(policyMapper.toResponse(policy)).thenReturn(expectedResponse);

        // Act
        PolicyResponse result = policyService.createPolicy(request);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getPolicyNumber(), result.getPolicyNumber());
        verify(policyRepository).save(policy);
    }

    @Test
    void createPolicy_InsuranceAlreadyHasPolicy_ThrowsException() {
        // Arrange
        UUID insuranceId = UUID.randomUUID();
        CreatePolicyRequest request = CreatePolicyRequest.builder()
                .insuranceId(insuranceId)
                .effectiveDate(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusYears(1))
                .build();

        Insurance insurance = new Insurance();
        insurance.setId(insuranceId);

        when(insuranceRepository.findById(insuranceId)).thenReturn(Optional.of(insurance));
        when(policyRepository.findByInsuranceId(insuranceId)).thenReturn(Optional.of(new Policy()));

        // Act & Assert
        assertThrows(ApiException.class, () -> policyService.createPolicy(request));
    }

    @Test
    void getPolicyById_Success() {
        // Arrange
        UUID policyId = UUID.randomUUID();
        Policy policy = new Policy();
        policy.setId(policyId);

        PolicyResponse expectedResponse = PolicyResponse.builder()
                .id(policyId)
                .build();

        when(policyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(policyMapper.toResponse(policy)).thenReturn(expectedResponse);

        // Act
        PolicyResponse result = policyService.getPolicyById(policyId);

        // Assert
        assertNotNull(result);
        assertEquals(policyId, result.getId());
    }

    @Test
    void getPolicyById_NotFound_ThrowsException() {
        // Arrange
        UUID policyId = UUID.randomUUID();
        when(policyRepository.findById(policyId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApiException.class, () -> policyService.getPolicyById(policyId));
    }
}
