package com.audiobook.voices;

import com.audiobook.voices.model.VoiceDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/voices")
public class VoicesHandler {

    private final VoicesService voicesService;

    public VoicesHandler(VoicesService voicesService) {
        this.voicesService = voicesService;
    }

    @GetMapping
    public Map<String, List<VoiceDetails>> getVoices() {
        return Map.of("voices", voicesService.listAllVoices());
    }
}
