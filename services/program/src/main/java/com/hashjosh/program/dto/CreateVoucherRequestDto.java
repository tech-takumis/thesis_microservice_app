package com.hashjosh.program.dto;

import com.hashjosh.program.enums.VoucherType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class CreateVoucherRequestDto {
    private UUID ownerUserId;
    private String title;
    private VoucherType voucherType;
    private String unit;
    private Integer quantity;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String referenceNumber;
}
