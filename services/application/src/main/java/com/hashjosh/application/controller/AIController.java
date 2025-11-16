package com.hashjosh.application.controller;

import com.hashjosh.application.service.AIService;
import com.hashjosh.constant.ai.AIResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AIController {

    private final AIService aiService;

    @GetMapping("/{applicationId}")
    public ResponseEntity<AIResultDTO> getAIResultByApplicationId(
            @PathVariable("applicationId") UUID applicationId
    ){
        AIResultDTO aiResult = aiService.getAIResultByApplicationId(applicationId.toString());
        return ResponseEntity.ok(aiResult);
    }
}
