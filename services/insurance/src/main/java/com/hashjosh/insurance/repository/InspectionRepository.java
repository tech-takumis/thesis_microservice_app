package com.hashjosh.insurance.repository;

import com.hashjosh.insurance.entity.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, UUID> {
}
