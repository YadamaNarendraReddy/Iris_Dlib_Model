package com.iris.assistant.domain;

public record FaceRecognitionResult(
        String faceId,
        String displayName,
        double confidence,
        boolean recognized
) {

    public static FaceRecognitionResult unknown(String faceId, double confidence) {
        return new FaceRecognitionResult(faceId, "Unknown guest", confidence, false);
    }
}
