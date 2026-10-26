package com.audiobook.generation;

import com.audiobook.shared.exception.AudiobookException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.Engine;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class AudioService {

    private final PollyClient pollyClient;
    private final S3Client s3Client;
    private final String generatedAudioBucket;

    public AudioService(PollyClient pollyClient,
                        S3Client s3Client,
                        @Value("${generated.audio.bucket}") String generatedAudioBucket) {
        this.pollyClient = pollyClient;
        this.s3Client = s3Client;
        this.generatedAudioBucket = generatedAudioBucket;
    }

    public String synthesiseAndStore(String jobId, String script, String voiceId, String engine) {
        try {
            ResponseInputStream<SynthesizeSpeechResponse> audioStream = pollyClient.synthesizeSpeech(
                    SynthesizeSpeechRequest.builder()
                            .text(script)
                            .voiceId(voiceId)
                            .engine(Engine.fromValue(engine))
                            .outputFormat(OutputFormat.MP3)
                            .build()
            );

            String s3Key = "jobs/" + jobId + "/audio.mp3";

            try (audioStream) {
                byte[] audioBytes = audioStream.readAllBytes();
                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(generatedAudioBucket)
                                .key(s3Key)
                                .contentType("audio/mpeg")
                                .build(),
                        RequestBody.fromBytes(audioBytes)
                );
            }

            return s3Key;

        } catch (Exception e) {
            throw new AudiobookException("Failed to synthesise or store audio", e);
        }
    }
}
