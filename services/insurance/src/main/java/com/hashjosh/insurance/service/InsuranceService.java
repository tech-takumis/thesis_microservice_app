package com.hashjosh.insurance.service;

import com.hashjosh.insurance.config.CustomUserDetails;
import com.hashjosh.insurance.dto.InsuranceFilter;
import com.hashjosh.insurance.dto.insurance.InsuranceRequestDTO;
import com.hashjosh.insurance.dto.insurance.InsuranceResponse;
import com.hashjosh.insurance.dto.insurance.InsuranceStatusStatisticDTO;
import com.hashjosh.insurance.entity.Insurance;
import com.hashjosh.insurance.exception.ApiException;
import com.hashjosh.insurance.mapper.InsuranceMapper;
import com.hashjosh.insurance.repository.InsuranceRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InsuranceService {

    private final InsuranceRepository insuranceRepository;
    private final InsuranceMapper insuranceMapper;
    @Transactional(readOnly = true)
    public List<InsuranceResponse> findAll(
    ) {
        return insuranceRepository.findAll().stream()
                .map(insuranceMapper::toInsuranceResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InsuranceResponse> findAllVerified() {
        return insuranceRepository.findByVerificationIsNotNull().stream()
                .map(insuranceMapper::toInsuranceResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InsuranceResponse findById(
            UUID insuranceId
    ) {
        Insurance insurance = insuranceRepository.findById(insuranceId)
                .orElseThrow(() -> ApiException.notFound("Insurance not found"));

        return insuranceMapper.toInsuranceResponse(insurance);
    }

    @Transactional(readOnly = true)
    public InsuranceResponse findByApplicationId(UUID applicationId) {
        Insurance insurance = insuranceRepository.findBySubmissionId(applicationId)
                .orElseThrow(() -> ApiException.notFound("Insurance not found"));

        return insuranceMapper.toInsuranceResponse(insurance);
    }

    @Transactional(readOnly = true)
    public List<InsuranceResponse> findByApplicationTypeId(UUID applicationTypeId) {

        List<Insurance> insurances = insuranceRepository.findByBatch_ApplicationTypeId(applicationTypeId);

        return insurances.stream()
                .map(insuranceMapper::toInsuranceResponse)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public InsuranceResponse update(UUID insuranceId, @Valid InsuranceRequestDTO request) {
        return null;
    }

    @Transactional(readOnly = true)
    public void delete(UUID insuranceId) {
        Insurance insurance = insuranceRepository.findById(insuranceId)
                .orElseThrow(() -> ApiException.notFound("Insurance not found"));

        insuranceRepository.delete(insurance);
    }

    public List<InsuranceResponse> findByCurrentUser() {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<Insurance> insurances = insuranceRepository.findByFarmerId(currentUser.getUserId());

        return insurances.stream()
                .map(insuranceMapper::toInsuranceResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InsuranceResponse> findByStatus(com.hashjosh.constant.pcic.enums.InsuranceStatus status) {
        List<Insurance> insurances = insuranceRepository.findByCurrentStatus(status);
        return insurances.stream()
                .map(insuranceMapper::toInsuranceResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InsuranceStatusStatisticDTO> getInsuranceStatisticsByStatus() {
        List<Object[]> results = insuranceRepository.countInsuranceByStatus();
        return results.stream()
                .map(obj -> new InsuranceStatusStatisticDTO(String.valueOf(obj[0]), (Long) obj[1]))
                .toList();
    }

    public String getInsuranceStatusBySubmissionId(UUID submissionId) {
        return insuranceRepository.findBySubmissionId(submissionId)
                .map(insurance -> insurance.getCurrentStatus().name())
                .orElseThrow(() -> ApiException.notFound("Insurance not found"));
    }
}
