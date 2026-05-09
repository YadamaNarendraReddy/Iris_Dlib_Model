package com.iris.assistant.service;

import com.iris.assistant.domain.ApplianceStatus;
import com.iris.assistant.domain.FaceRecognitionResult;
import com.iris.assistant.domain.IntentResult;
import com.iris.assistant.domain.IntentType;
import org.springframework.stereotype.Service;

@Service
public class ResponseGenerator {

    public String generate(FaceRecognitionResult face, IntentResult intent, boolean authenticated) {
        String name = face.recognized() ? face.displayName() : "there";

        if (intent.type() == IntentType.GENERAL_CHAT) {
            return "I am good, %s. Ready when you are.".formatted(name);
        }
        if (!face.recognized()) {
            return "Sorry, I could not verify who is speaking. Please move closer to the camera and try again.";
        }
        if (!authenticated) {
            return "Sorry, %s, you are not authenticated to control that appliance.".formatted(name);
        }
        if (intent.targetStatus() == ApplianceStatus.ON) {
            return "Hey %s, I have got it. The lights are now ON.".formatted(name);
        }
        if (intent.targetStatus() == ApplianceStatus.OFF) {
            return "Hey %s, I have got it. The lights are now OFF.".formatted(name);
        }
        return "I heard you, %s, but I do not know how to handle that request yet.".formatted(name);
    }
}
