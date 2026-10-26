package com.audiobook.generation;

import com.audiobook.generation.model.GenerateRequest;
import com.audiobook.generation.model.GenerateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/generate")
public class GenerationHandler {

    private final GenerationService generationService;

    public GenerationHandler(GenerationService generationService) {
        this.generationService = generationService;
    }

    @PostMapping
    public ResponseEntity<GenerateResponse> generate(@RequestBody GenerateRequest request) {
        String jobId = generationService.startGeneration(
                request.getPrompt(),
                request.getLength(),
                request.getStyle(),
                request.getVoiceId(),
                request.getEngine()
        );

        return ResponseEntity.accepted()
                .body(GenerateResponse.builder().jobId(jobId).build());
    }
}
