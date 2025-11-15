package com.hashjosh.insurance.clients;

import com.hashjosh.constant.ai.AIResultDTO;
import com.hashjosh.insurance.exception.ApiException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class AIClient {

    private final RestClient restClient;

    public AIClient(RestClient.Builder clientBuilder) {
        this.restClient = clientBuilder
                .baseUrl("http://localhost:8000")
                .build();
    }

    public AIResultDTO getAIResultByApplicationId(String applicationId){
        return Objects.requireNonNull(restClient.get()
                        .uri("/ai/applications/{application_id}", applicationId)
                        .retrieve()
                        .onStatus(
                                status -> status.is4xxClientError() || status.is5xxServerError(),
                                (request, res) -> {
                                    log.error("Failed to fetch AI result for application id: {}, status code: {}", applicationId, res.getStatusCode());
                                    throw ApiException.internalError("Failed to fetch AI result for application id");
                                }
                        )
                        .body(SingleResultWrapper.class))
                .getData();
    }

    @Setter
    @Getter
    public static class SingleResultWrapper {
        private boolean success;
        private AIResultDTO data;
        private String message;
    }

}
