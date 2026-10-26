package com.audiobook.generation;

import com.audiobook.jobs.JobRepository;
import com.audiobook.jobs.model.Job;
import com.audiobook.shared.exception.AudiobookException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class GenerationService {

    private final ScriptService scriptService;
    private final AudioService audioService;
    private final JobRepository jobRepository;

    public GenerationService(ScriptService scriptService,
                             AudioService audioService,
                             JobRepository jobRepository) {
        this.scriptService = scriptService;
        this.audioService = audioService;
        this.jobRepository = jobRepository;
    }

    public String startGeneration(String prompt, String length, String style,
                                  String voiceId, String engine) {
        String jobId = UUID.randomUUID().toString();
        String now = Instant.now().toString();
        long expiresAt = Instant.now().plus(24, ChronoUnit.HOURS).getEpochSecond();

        Job job = Job.builder()
                .jobId(jobId)
                .status("QUEUED")
                .stage("QUEUED")
                .createdAt(now)
                .updatedAt(now)
                .expiresAt(expiresAt)
                .build();

        jobRepository.save(job);

        try {
            jobRepository.updateStage(jobId, "IN_PROGRESS", "WRITING_SCRIPT");
            String script = scriptService.generateScript(prompt, length, style);

            jobRepository.updateStage(jobId, "IN_PROGRESS", "GENERATING_AUDIO");
            String audioKey = audioService.synthesiseAndStore(jobId, script, voiceId, engine);

            jobRepository.updateComplete(jobId, script, audioKey);

        } catch (AudiobookException e) {
            jobRepository.updateFailed(jobId, e.getMessage());
        } catch (Exception e) {
            jobRepository.updateFailed(jobId, "An unexpected error occurred");
        }

        return jobId;
    }
}
