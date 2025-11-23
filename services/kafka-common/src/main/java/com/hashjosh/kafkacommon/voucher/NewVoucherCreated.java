package com.hashjosh.kafkacommon.voucher;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewVoucherCreated {
    private UUID voucherId;
    private String code;
    private UUID ownerUserId;
    private String title;
    private String voucherType;
    private String unit;
    private Integer quantity;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String referenceNumber;
}
