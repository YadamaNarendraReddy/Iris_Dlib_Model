package com.iris.assistant.api;

public record InteractionRequest(
        String transcript,
        String faceId,
        Double confidence
) {
}
