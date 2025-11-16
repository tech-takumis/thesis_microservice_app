package com.hashjosh.application.service;

import com.hashjosh.application.clients.AIClient;
import com.hashjosh.constant.ai.AIResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIService {

    private final AIClient aiClient;

    public AIResultDTO getAIResultByApplicationId(String applicationId){
        return aiClient.getAIResultByApplicationId(applicationId);
    }
}
