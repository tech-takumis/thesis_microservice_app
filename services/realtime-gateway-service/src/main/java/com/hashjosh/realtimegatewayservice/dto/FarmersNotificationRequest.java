package com.hashjosh.realtimegatewayservice.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmersNotificationRequest {
    private List<UUID> farmersIds;
    private String title;
    private String message;
}
