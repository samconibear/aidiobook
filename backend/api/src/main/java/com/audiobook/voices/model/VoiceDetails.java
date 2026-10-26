package com.audiobook.voices.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VoiceDetails {
    private String id;
    private String name;
    private String gender;
    private String engine;
    private float pricePerM;
    private String languageCode;
    private String languageName;
}
