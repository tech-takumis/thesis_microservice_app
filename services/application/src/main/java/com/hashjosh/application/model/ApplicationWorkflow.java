package com.hashjosh.application.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@Table(name = "application_workflows")
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationWorkflow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    private boolean verificationEnabled;

    private boolean inspectionEnabled;
    private boolean policyEnabled ;
    private boolean claimEnabled;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "applicationWorkflow")
    private ApplicationType applicationType;
}
