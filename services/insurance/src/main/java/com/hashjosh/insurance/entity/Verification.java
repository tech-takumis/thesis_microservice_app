package com.hashjosh.insurance.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.hashjosh.insurance.dto.verification.VerificationResponse;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Verification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID verifierId;

    private String verifierName;

    private String remarks;

    private List<UUID> verificationDocuments;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb",name = "verified_field_values")
    private JsonNode fieldValues;

    private LocalDateTime verifiedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_id", nullable = false)
    private Insurance insurance;
}
