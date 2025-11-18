package com.hashjosh.insurance.clients;

import com.hashjosh.constant.document.dto.DocumentResponse;
import com.hashjosh.insurance.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Slf4j
public class DocumentServiceClient {

    private final RestClient restClient;

    @Value("${spring.application.name}")
    private String applicationName;

    public DocumentServiceClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://localhost:8020/api/v1/documents")
                .build();

    }

    public String generatePresignedUrl(UUID userId,UUID documentId, int expiry) {
        return restClient.get()
                .uri("/{id}/download-url?expiryMinutes={expiry}", documentId, expiry)
                .header("X-Internal-Service", applicationName)
                .header("X-User-Id", String.valueOf(userId))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        (request, response) -> {
                            throw ApiException.internalError("Failed to generate presigned url for document: " + documentId);
                        }
                )
                .body(String.class);
    }

    public DocumentResponse uploadDocument(MultipartFile file, String userId) {
        try {
            log.info("Uploading document: {} for user: {}", file.getOriginalFilename(), userId);
            log.info("File size: {} bytes, Content-Type: {}", file.getSize(), file.getContentType());
            log.info("Sending headers: X-Internal-Service={}, X-User-Id={}", applicationName, userId);

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(file.getBytes()) {
                        @Override
                        public String getFilename() {
                            return file.getOriginalFilename();
                        }
                    })
                    .header("Content-Type", file.getContentType());

            return restClient.post()
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(builder.build())
                    .header("X-Internal-Service", applicationName)
                    .header("X-User-Id", userId)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            (request, response) -> {
                                String responseBody = "";
                                try {
                                    responseBody = new String(response.getBody().readAllBytes());
                                    log.error("Document service error response: Status={}, Body={}", response.getStatusCode(), responseBody);
                                } catch (Exception e) {
                                    log.error("Failed to read error response body", e);
                                }
                                throw ApiException.internalError("Failed to upload document: " + file.getOriginalFilename() +
                                    ". Status: " + response.getStatusCode() + ", Response: " + responseBody);
                            }
                    )
                    .body(DocumentResponse.class);
        } catch (ApiException e) {
            throw e;
        } catch (RestClientException e) {
            log.error("RestClient error uploading document: {}", file.getOriginalFilename(), e);
            throw ApiException.internalError("Failed to upload document due to connection error: " + e.getMessage());
        } catch (java.io.IOException e) {
            log.error("IO error reading file: {}", file.getOriginalFilename(), e);
            throw ApiException.internalError("Failed to read file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error uploading document: {}", file.getOriginalFilename(), e);
            throw ApiException.internalError("Unexpected error uploading document: " + e.getMessage());
        }
    }
}
