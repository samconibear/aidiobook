package com.audiobook.jobs.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobResponse {
    private String jobId;
    private String status;
    private String stage;
    private String script;
    private String audioUrl;
    private String error;
}
