package com.iris.assistant.service;

import com.iris.assistant.domain.FaceRecognitionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FaceRecognitionService {

    private final UserDirectory userDirectory;
    private final double threshold;

    public FaceRecognitionService(
            UserDirectory userDirectory,
            @Value("${iris.face-match-threshold:0.72}") double threshold
    ) {
        this.userDirectory = userDirectory;
        this.threshold = threshold;
    }

    public FaceRecognitionResult recognize(String faceId, Double confidenceInput) {
        double confidence = confidenceInput == null ? 0.88 : Math.max(0, Math.min(1, confidenceInput));
        return userDirectory.findByFaceId(faceId)
                .filter(user -> confidence >= threshold)
                .map(user -> new FaceRecognitionResult(user.faceId(), user.displayName(), confidence, true))
                .orElseGet(() -> FaceRecognitionResult.unknown(faceId, confidence));
    }
}
