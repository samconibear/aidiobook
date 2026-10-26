package com.audiobook.voices;

import com.audiobook.voices.model.VoiceDetails;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.DescribeVoicesRequest;
import software.amazon.awssdk.services.polly.model.DescribeVoicesResponse;
import software.amazon.awssdk.services.polly.model.Engine;
import software.amazon.awssdk.services.polly.model.Voice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class VoicesService {
    private static record EngineData(float dollarPerMillionChars) {}
    private static final Map<Engine, EngineData> ENGINES = Map.of(
        Engine.STANDARD, new EngineData(4),
        Engine.NEURAL, new EngineData(16),
        Engine.LONG_FORM, new EngineData(30),
        Engine.GENERATIVE,  new EngineData(100)
    );

    private final PollyClient pollyClient;

    public VoicesService(PollyClient pollyClient) {
        this.pollyClient = pollyClient;
    }

    public List<VoiceDetails> listAllVoices() {
        List<VoiceDetails> voices = new ArrayList<>();

        ENGINES.forEach((engine, pricing) -> {
            DescribeVoicesResponse response = pollyClient.describeVoices(
                    DescribeVoicesRequest.builder()
                            .engine(engine)
                            .build()
            );
            for (Voice voice : response.voices()) {
                voices.add(VoiceDetails.builder()
                        .id(voice.id().toString())
                        .name(voice.name())
                        .gender(voice.genderAsString())
                        .engine(engine.toString().toLowerCase())
                        .pricePerM(pricing.dollarPerMillionChars())
                        .languageCode(voice.languageCodeAsString())
                        .languageName(voice.languageName())
                        .build());
            }
        });

        return voices;
    }
}
