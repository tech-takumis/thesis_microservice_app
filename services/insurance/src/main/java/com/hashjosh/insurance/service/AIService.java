package com.hashjosh.insurance.service;

import com.hashjosh.constant.ai.AIResultDTO;
import com.hashjosh.insurance.clients.AIClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIService {

    private final AIClient aiClient;

    public AIResultDTO getAIResultByInsuranceId(String submissionId) {
        return aiClient.getAIResultByApplicationId(submissionId);
    }
}
