package com.hashjosh.program.entity;

import com.hashjosh.program.enums.VoucherStatus;
import com.hashjosh.program.enums.VoucherType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code; // QR/Barcode scannable unique identifier

    private UUID ownerUserId;

    private String title;

    @Enumerated(EnumType.STRING)
    private VoucherType voucherType;

    private String unit; // "bags", "kg", "pieces", etc.

    private Integer quantity;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VoucherStatus status = VoucherStatus.ISSUED;

    private LocalDateTime claimedAt;

    private UUID claimedByUserId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
