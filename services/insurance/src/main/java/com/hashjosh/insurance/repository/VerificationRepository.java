package com.hashjosh.insurance.repository;

import com.hashjosh.insurance.entity.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, UUID> {
    Optional<Verification> findByInsuranceId(UUID insuranceId);
}
