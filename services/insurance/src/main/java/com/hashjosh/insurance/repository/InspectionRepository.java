package com.hashjosh.insurance.repository;

import com.hashjosh.insurance.entity.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, UUID> {

    Optional<Inspection> findByInsurance_Id(UUID insuranceId);

    List<Inspection> findByInspectorId(UUID inspectorId);

    @Query("SELECT i FROM Inspection i JOIN FETCH i.insurance WHERE i.scheduleId = :scheduleId")
    Optional<Inspection> findByScheduleIdWithInsurance(@Param("scheduleId") UUID scheduleId);

    boolean existsByInsurance_Id(UUID insuranceId);
}
