package com.hashjosh.application.clients;

import com.hashjosh.application.exceptions.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
@Slf4j
public class InsuranceClient {

    private final String BASE_URL = "http://localhost:8050/api/v1/insurance";
    private final RestClient restClient;

    @Value("${spring.application.name}")
    private String applicationName;

    public InsuranceClient(RestClient.Builder builder){
        this.restClient = builder
                .baseUrl(BASE_URL)
                .build();
    }

    public String getInsuranceStatus(UUID applicationId,UUID userId){
        return restClient.get()
                .uri(BASE_URL+"/application/status/{applicationId}",applicationId)
                .header("X-Internal-Service", applicationName)
                .header("X-User-Id", String.valueOf(userId))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        (request, res) -> {
                            log.error("Failed to fetch Insurance");
                            throw ApiException.internalError("Insurance service is unavailable");
                        }
                )
                .body(String.class);
    }

}
