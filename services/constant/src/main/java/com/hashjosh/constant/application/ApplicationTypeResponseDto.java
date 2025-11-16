package com.hashjosh.constant.application;

import com.hashjosh.constant.workflow.ApplicationWorkflowResponse;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationTypeResponseDto{
    private UUID id;
    private String name;
    private String description;
    private String provider;
    private String layout;
    private Boolean printable;
    private ApplicationWorkflowResponse workflow;
    private List<ApplicationResponseDto> applications;
}
