package com.hashjosh.program.dto;

import com.hashjosh.program.enums.VoucherStatus;
import com.hashjosh.program.enums.VoucherType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class VoucherResponseDto {
    private UUID id;
    private String code;
    private UUID ownerUserId;
    private String title;
    private VoucherType voucherType;
    private String unit;
    private Integer quantity;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String referenceNumber;
    private VoucherStatus status;
    private LocalDateTime claimedAt;
    private UUID claimedByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
