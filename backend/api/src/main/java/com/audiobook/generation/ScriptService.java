package com.audiobook.generation;

import com.audiobook.shared.exception.AudiobookException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Service
public class ScriptService {

    private final BedrockRuntimeClient bedrockClient;
    private final String modelId;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ScriptService(BedrockRuntimeClient bedrockClient,
                         @Value("${bedrock.model.id}") String modelId) {
        this.bedrockClient = bedrockClient;
        this.modelId = modelId;
    }

    public String generateScript(String prompt, String length, String style) {
        String systemPrompt = buildSystemPrompt(length, style);
        String userMessage = "Create an audiobook script about: " + prompt;

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("anthropic_version", "bedrock-2023-05-31");
            requestBody.put("max_tokens", maxTokensFor(length));

            ArrayNode systemArray = objectMapper.createArrayNode();
            ObjectNode systemMsg = objectMapper.createObjectNode();
            systemMsg.put("type", "text");
            systemMsg.put("text", systemPrompt);
            systemArray.add(systemMsg);
            requestBody.set("system", systemArray);

            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            ArrayNode contentArray = objectMapper.createArrayNode();
            ObjectNode contentItem = objectMapper.createObjectNode();
            contentItem.put("type", "text");
            contentItem.put("text", userMessage);
            contentArray.add(contentItem);
            userMsg.set("content", contentArray);
            messages.add(userMsg);
            requestBody.set("messages", messages);

            InvokeModelResponse response = bedrockClient.invokeModel(InvokeModelRequest.builder()
                    .modelId(modelId)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(objectMapper.writeValueAsString(requestBody)))
                    .build());

            ObjectNode responseBody = objectMapper.readValue(
                    response.body().asUtf8String(), ObjectNode.class);

            return responseBody
                    .get("content")
                    .get(0)
                    .get("text")
                    .asText();

        } catch (Exception e) {
            throw new AudiobookException("Failed to generate script", e);
        }
    }

    private String buildSystemPrompt(String length, String style) {
        String wordTarget = switch (length.toUpperCase()) {
            case "SHORT" -> "approximately 800 words (suitable for a 5-minute audiobook)";
            case "LONG" -> "approximately 4000 words (suitable for a 30-minute audiobook)";
            default -> "approximately 2000 words (suitable for a 15-minute audiobook)";
        };

        String toneGuidance = switch (style.toUpperCase()) {
            case "DOCUMENTARY" -> "Write in a factual, authoritative documentary style. Present information clearly and objectively, as if narrating a high-quality documentary film.";
            case "NARRATIVE" -> "Write in an engaging narrative storytelling style. Use vivid descriptions, compelling character moments, and draw the listener in with a strong narrative voice.";
            default -> "Write in a clear, structured educational style. Explain concepts progressively, use examples, and ensure the listener comes away with a solid understanding.";
        };

        return String.format(
                "You are a professional audiobook script writer. Write a script of %s. %s " +
                "Write the script as continuous prose suitable for reading aloud — no stage directions, " +
                "no chapter markers, no metadata. Begin immediately with the content.",
                wordTarget, toneGuidance
        );
    }

    private int maxTokensFor(String length) {
        return switch (length.toUpperCase()) {
            case "SHORT" -> 1200;
            case "LONG" -> 6000;
            default -> 3000;
        };
    }
}
