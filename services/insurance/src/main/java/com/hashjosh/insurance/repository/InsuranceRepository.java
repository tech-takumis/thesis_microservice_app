package com.hashjosh.insurance.repository;

import com.hashjosh.insurance.entity.Insurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InsuranceRepository extends JpaRepository<Insurance, UUID> {

    List<Insurance> findByBatch_ApplicationTypeId(UUID batchApplicationTypeId);

}
