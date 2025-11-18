package com.hashjosh.application.dto.update;

import lombok.*;

import java.util.UUID;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUpdateResponse {
    private UUID applicationId;
    private boolean success;
    private String message;
    private Long version;
}
