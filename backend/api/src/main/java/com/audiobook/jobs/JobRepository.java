package com.audiobook.jobs;

import com.audiobook.jobs.model.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class JobRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public JobRepository(DynamoDbClient dynamoDbClient,
                         @Value("${jobs.table.name}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    public void save(Job job) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("jobId", str(job.getJobId()));
        item.put("status", str(job.getStatus()));
        item.put("stage", str(job.getStage()));
        item.put("createdAt", str(job.getCreatedAt()));
        item.put("updatedAt", str(job.getUpdatedAt()));
        item.put("expiresAt", num(String.valueOf(job.getExpiresAt())));
        putIfPresent(item, "script", job.getScript());
        putIfPresent(item, "audioKey", job.getAudioKey());
        putIfPresent(item, "error", job.getError());

        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build());
    }

    public Optional<Job> findById(String jobId) {
        GetItemResponse response = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("jobId", str(jobId)))
                .build());

        if (!response.hasItem()) {
            return Optional.empty();
        }

        Map<String, AttributeValue> item = response.item();
        return Optional.of(Job.builder()
                .jobId(getString(item, "jobId"))
                .status(getString(item, "status"))
                .stage(getString(item, "stage"))
                .script(getString(item, "script"))
                .audioKey(getString(item, "audioKey"))
                .error(getString(item, "error"))
                .createdAt(getString(item, "createdAt"))
                .updatedAt(getString(item, "updatedAt"))
                .build());
    }

    public void updateStage(String jobId, String status, String stage) {
        String now = Instant.now().toString();
        dynamoDbClient.updateItem(UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("jobId", str(jobId)))
                .updateExpression("SET #s = :status, stage = :stage, updatedAt = :updatedAt")
                .expressionAttributeNames(Map.of("#s", "status"))
                .expressionAttributeValues(Map.of(
                        ":status", str(status),
                        ":stage", str(stage),
                        ":updatedAt", str(now)
                ))
                .build());
    }

    public void updateComplete(String jobId, String script, String audioKey) {
        String now = Instant.now().toString();
        dynamoDbClient.updateItem(UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("jobId", str(jobId)))
                .updateExpression("SET #s = :status, stage = :stage, script = :script, audioKey = :audioKey, updatedAt = :updatedAt")
                .expressionAttributeNames(Map.of("#s", "status"))
                .expressionAttributeValues(Map.of(
                        ":status", str("COMPLETE"),
                        ":stage", str("COMPLETE"),
                        ":script", str(script),
                        ":audioKey", str(audioKey),
                        ":updatedAt", str(now)
                ))
                .build());
    }

    public void updateFailed(String jobId, String error) {
        String now = Instant.now().toString();
        dynamoDbClient.updateItem(UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("jobId", str(jobId)))
                .updateExpression("SET #s = :status, stage = :stage, #e = :error, updatedAt = :updatedAt")
                .expressionAttributeNames(Map.of("#s", "status", "#e", "error"))
                .expressionAttributeValues(Map.of(
                        ":status", str("FAILED"),
                        ":stage", str("FAILED"),
                        ":error", str(error),
                        ":updatedAt", str(now)
                ))
                .build());
    }

    private static AttributeValue str(String value) {
        return AttributeValue.builder().s(value).build();
    }

    private static AttributeValue num(String value) {
        return AttributeValue.builder().n(value).build();
    }

    private static void putIfPresent(Map<String, AttributeValue> item, String key, String value) {
        if (value != null && !value.isBlank()) {
            item.put(key, str(value));
        }
    }

    private static String getString(Map<String, AttributeValue> item, String key) {
        AttributeValue val = item.get(key);
        return val != null ? val.s() : null;
    }
}
