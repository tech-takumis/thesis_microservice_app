package com.hashjosh.insurance.repository;

import com.hashjosh.insurance.entity.Claim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {

    Optional<Claim> findByInsuranceId(UUID insuranceId);

    Page<Claim> findByInsuranceId(UUID insuranceId, Pageable pageable);
}
