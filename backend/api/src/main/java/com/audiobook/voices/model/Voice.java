package com.audiobook.voices.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Voice {
    private String id;
    private String name;
    private String gender;
    private String engine;
    private Integer pricing;
    private String languageCode;
    private String languageName;
}
