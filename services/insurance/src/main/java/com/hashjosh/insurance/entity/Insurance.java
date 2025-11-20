package com.hashjosh.insurance.entity;

import com.hashjosh.constant.pcic.enums.InsuranceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "insurances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Insurance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID submissionId;
    private UUID applicationTypeId;
    private String applicationTypeName;
    private UUID farmerId;

    private String farmerName;

    @Enumerated(EnumType.STRING)
    private InsuranceStatus currentStatus;

    private Boolean isAIProcessed;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "insurance", cascade = CascadeType.ALL)
    private Verification verification;

    @OneToOne(mappedBy = "insurance", cascade = CascadeType.ALL)
    private Policy policy;

    @OneToOne(mappedBy = "insurance", cascade = CascadeType.ALL)
    private Claim claim;

    @OneToOne(mappedBy = "insurance", cascade = CascadeType.ALL)
    private Inspection inspection;
}
