package com.hashjosh.insurance.repository;

import com.hashjosh.insurance.entity.Insurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InsuranceRepository extends JpaRepository<Insurance, UUID> {

    List<Insurance> findByBatch_ApplicationTypeId(UUID batchApplicationTypeId);

    Optional<Insurance> findBySubmissionId(UUID submissionId);

    List<Insurance> findByVerificationIsNotNull();

    List<Insurance> findByFarmerId(UUID userId);

    List<Insurance> findByCurrentStatus(com.hashjosh.constant.pcic.enums.InsuranceStatus status);

    @Query("SELECT i.currentStatus as status, COUNT(i) as count FROM Insurance i GROUP BY i.currentStatus")
    List<Object[]> countInsuranceByStatus();
}
