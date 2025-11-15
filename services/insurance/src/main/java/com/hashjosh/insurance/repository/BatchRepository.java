package com.hashjosh.insurance.repository;
import com.hashjosh.insurance.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BatchRepository extends JpaRepository<Batch, UUID> {
    
    default List<Batch> findAllAvailableBatchesByApplicationTypeId(UUID applicationTypeId) {
        LocalDateTime now = LocalDateTime.now();
        return findByApplicationTypeIdAndStartDateBeforeAndEndDateAfter(applicationTypeId, now, now);
    }

    List<Batch> findByApplicationTypeIdAndStartDateBeforeAndEndDateAfter(UUID applicationTypeId, LocalDateTime now, LocalDateTime now1);
}
