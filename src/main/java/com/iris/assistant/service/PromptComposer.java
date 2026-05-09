package com.iris.assistant.service;

import com.iris.assistant.domain.Appliance;
import com.iris.assistant.domain.FaceRecognitionResult;
import com.iris.assistant.domain.IntentResult;
import org.springframework.stereotype.Service;

@Service
public class PromptComposer {

    public String compose(
            String transcript,
            FaceRecognitionResult face,
            IntentResult intent,
            boolean authenticated,
            Appliance appliance
    ) {
        String applianceStatus = appliance == null ? "N/A" : appliance.status().name();
        return """
                You are Iris, a home AI assistant.
                Use the recognized username in the answer when available.
                Context:
                - username: %s
                - faceRecognized: %s
                - userAuth: %s
                - intent: %s
                - applianceStatus: %s
                User said: %s
                Generate a concise spoken response.
                """.formatted(
                face.displayName(),
                face.recognized(),
                authenticated,
                intent.type(),
                applianceStatus,
                transcript
        ).trim();
    }
}
