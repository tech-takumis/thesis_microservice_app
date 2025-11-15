package com.hashjosh.insurance.clients;

import com.hashjosh.constant.application.ApplicationTypeResponseDto;
import com.hashjosh.insurance.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@Slf4j
public class ApplicationTypeClient {

    private final RestClient restClient;
    @Value("${spring.application.name}")
    private String applicationName;

    public ApplicationTypeClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://localhost:8010/api/v1/application/type")
                .build();

    }

    public List<ApplicationTypeResponseDto> getAllApplicationTypes(String userId) {
        return restClient.get()
                .uri("")
                .header("X-Internal-Service", applicationName)
                .header("X-User-Id", userId)
                .exchange((req, res) -> {
                    if(res.getStatusCode().is2xxSuccessful()) {
                        return res.bodyTo(new ParameterizedTypeReference<List<ApplicationTypeResponseDto>>(){});
                    } else {
                        throw ApiException.internalError("Remote Application Service error in fetching all application types: status"+ res.getStatusCode());
                    }
                });
    }

}
