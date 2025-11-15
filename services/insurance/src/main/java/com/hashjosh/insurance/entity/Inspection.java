package com.hashjosh.insurance.entity;


import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "inspections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID inspectorId;

    private String inspectorName;

    private LocalDateTime inspectedAt;

    private List<UUID> photos;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb",name = "inspection_field_values")
    private JsonNode fieldValues;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_id", nullable = false)
    private Insurance insurance;
}
