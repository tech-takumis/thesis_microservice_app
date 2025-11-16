package com.hashjosh.application.clients;

import com.hashjosh.constant.farmer.FarmerReponse;
import com.hashjosh.application.exceptions.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
@Slf4j
public class FarmerServiceClient {

    private final RestClient restClient;
    private static final String BASE_URL = "http://localhost:9020/api/v1/farmer";
    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service";
    private static final String USERID_HEADER = "X-User-Id";

    @Value("${spring.application.name}")
    private String applicationName;

    public FarmerServiceClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl(BASE_URL)
                .build();
    }

    public FarmerReponse getFarmerById(UUID farmerId, UUID userId) {
        try {
            FarmerReponse response = restClient.get()
                    .uri("/{farmer-id}", farmerId)
                    .header(INTERNAL_SERVICE_HEADER, applicationName)
                    .header(USERID_HEADER, String.valueOf(userId))
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            (request, res) -> {
                                log.error("Failed to fetch farmer with id: {}, status code: {}", farmerId, res.getStatusCode());
                                throw ApiException.internalError("Failed to fetch farmer with id: " + farmerId);
                            }
                    )
                    .body(FarmerReponse.class);

            log.info("Successfully fetched farmer: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error fetching farmer with id {}: {}", farmerId, e.getMessage());
            throw new RuntimeException("Failed to fetch farmer with id: " + farmerId, e);
        }
    }
}
