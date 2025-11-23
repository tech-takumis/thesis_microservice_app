package com.hashjosh.kafkacommon.voucher;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherClaimedEvent {
    private UUID voucherId;
    private UUID ownerUserId;
    private String voucherCode;
    private String title;
    private UUID claimedByUserId;
    private LocalDateTime claimedAt;
}
