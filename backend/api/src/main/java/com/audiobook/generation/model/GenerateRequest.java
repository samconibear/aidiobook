package com.audiobook.generation.model;

import lombok.Data;

@Data
public class GenerateRequest {
    private String prompt;
    private String length;
    private String style;
    private String voiceId;
    private String engine;
}
