package com.hashjosh.application.clients;

import com.hashjosh.application.exceptions.ApiException;
import com.hashjosh.constant.policy.PolicyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class PolicyClient {
    private final RestClient restClient;

    private static final String BASE_URL = "http://localhost:8050/api/v1/policies";
    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service";
    private static final String USERID_HEADER = "X-User-Id";

    @Value("${spring.application.name}")
    private String applicationName;

    public PolicyClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl(BASE_URL)
                .build();
    }

    public PolicyResponse getPolicyByPolicyNumber(String policyNumber, String userId) {
       try{
           return restClient.get()
                   .uri("/policy-number/{policyNumber}", policyNumber)
                   .header(INTERNAL_SERVICE_HEADER, applicationName)
                   .header(USERID_HEADER, userId)
                   .retrieve()
                   .onStatus(
                           status -> status.is4xxClientError() || status.is5xxServerError(),
                           (request, res) -> {
                               log.error("Failed to fetch farmer with id: {}, status code: {}", policyNumber, res.getStatusCode());
                               throw ApiException.internalError("Failed to fetch farmer with id: ,"+ policyNumber+ " status code: "+ res.getStatusCode());
                           }
                   )
                   .body(PolicyResponse.class);
       }catch (Exception e){
              log.error("Error fetching policy with policy number {}: {}", policyNumber, e.getMessage());
              throw ApiException.badRequest("Failed to fetch policy with policy number: " + policyNumber);
       }
    }
}
