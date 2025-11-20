package com.hashjosh.insurance.repository;

import com.hashjosh.insurance.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, UUID> {

    @Query("SELECT p FROM Policy p WHERE p.insurance.id = :insuranceId")
    Optional<Policy> findByInsuranceId(@Param("insuranceId") UUID insuranceId);

    @Query("SELECT p FROM Policy p WHERE p.policyNumber = :policyNumber")
    Optional<Policy> findByPolicyNumber(@Param("policyNumber") String policyNumber);

    @Query("SELECT p FROM Policy p WHERE p.insurance.farmerId = :farmerId")
    List<Policy> findByFarmerId(@Param("farmerId") UUID farmerId);

    @Query("SELECT p FROM Policy p WHERE p.effectiveDate <= CURRENT_TIMESTAMP AND p.expiryDate >= CURRENT_TIMESTAMP")
    List<Policy> findActivePolicies();

    @Query("SELECT p FROM Policy p WHERE p.expiryDate < CURRENT_TIMESTAMP")
    List<Policy> findExpiredPolicies();
}
