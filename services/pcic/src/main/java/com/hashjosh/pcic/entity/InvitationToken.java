package com.hashjosh.pcic.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invitation_tokens")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class InvitationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true)
    private String token;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private LocalDateTime expiryDate;
    @Column(nullable = false)
    private boolean used;
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }
}

