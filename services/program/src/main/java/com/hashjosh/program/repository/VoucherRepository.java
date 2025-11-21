package com.hashjosh.program.repository;

import com.hashjosh.program.entity.Voucher;
import com.hashjosh.program.enums.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, UUID> {
    Optional<Voucher> findByCode(String code);
    List<Voucher> findByOwnerUserId(UUID ownerUserId);
    List<Voucher> findByStatus(VoucherStatus status);


    @Query("SELECT v FROM Voucher v WHERE v.ownerUserId = :userId")
    List<Voucher> findAllByOwnerUserId(@Param("userId")UUID userId);
}
