package com.audiobook.jobs;

import com.audiobook.jobs.model.Job;
import com.audiobook.jobs.model.JobResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;

@RestController
@RequestMapping("/jobs")
public class JobHandler {

    private final JobRepository jobRepository;
    private final S3Presigner s3Presigner;
    private final String generatedAudioBucket;
    private final long presignedUrlExpiryMinutes;

    public JobHandler(JobRepository jobRepository,
                      S3Presigner s3Presigner,
                      @Value("${generated.audio.bucket}") String generatedAudioBucket,
                      @Value("${presigned.url.expiry.minutes:60}") long presignedUrlExpiryMinutes) {
        this.jobRepository = jobRepository;
        this.s3Presigner = s3Presigner;
        this.generatedAudioBucket = generatedAudioBucket;
        this.presignedUrlExpiryMinutes = presignedUrlExpiryMinutes;
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable String jobId) {
        return jobRepository.findById(jobId)
                .map(job -> ResponseEntity.ok(toResponse(job)))
                .orElse(ResponseEntity.notFound().build());
    }

    private JobResponse toResponse(Job job) {
        String audioUrl = null;
        if ("COMPLETE".equals(job.getStatus()) && job.getAudioKey() != null) {
            audioUrl = s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(presignedUrlExpiryMinutes))
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(generatedAudioBucket)
                            .key(job.getAudioKey())
                            .build())
                    .build())
                    .url()
                    .toString();
        }

        return JobResponse.builder()
                .jobId(job.getJobId())
                .status(job.getStatus())
                .stage(job.getStage())
                .script(job.getScript())
                .audioUrl(audioUrl)
                .error(job.getError())
                .build();
    }
}
