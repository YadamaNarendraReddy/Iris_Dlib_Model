package com.iris.assistant.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record InteractionResult(
        String requestId,
        Instant timestamp,
        String transcript,
        FaceRecognitionResult face,
        IntentResult intent,
        boolean authenticated,
        Appliance appliance,
        String prompt,
        String assistantResponse,
        String ttsText,
        List<PipelineStep> pipeline,
        Map<String, Object> context
) {
}
