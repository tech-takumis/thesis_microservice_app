package com.hashjosh.farmer.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
public class RsbsaServiceClient {

    private final RestClient restClient;

    @Value("${spring.application.name}")
    private String applicationName;

    public RsbsaServiceClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://rsbsa-service/api/v1/public/rsbsa")
                .build();
    }

    public RsbsaResponseDto getRsbsaById(String rsbsaId) {
        return restClient.get()
                .uri("/{rsbsa-id}", rsbsaId)
                .retrieve()
                .body(RsbsaResponseDto.class);
    }

    public Boolean existsById(String rsbsaId) {
        return restClient.get()
                .uri("/{rsbsa-id}/exists", rsbsaId)
                .header("X-Internal-Service", applicationName)
                .retrieve()
                .body(Boolean.class);

    }
}
