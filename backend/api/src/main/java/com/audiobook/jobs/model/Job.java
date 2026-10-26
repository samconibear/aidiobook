package com.audiobook.jobs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    private String jobId;
    private String status;
    private String stage;
    private String script;
    private String audioKey;
    private String error;
    private String createdAt;
    private String updatedAt;
    private long expiresAt;
}
