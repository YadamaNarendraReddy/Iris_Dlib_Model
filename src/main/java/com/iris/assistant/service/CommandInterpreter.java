package com.iris.assistant.service;

import com.iris.assistant.domain.ApplianceStatus;
import com.iris.assistant.domain.IntentResult;
import com.iris.assistant.domain.IntentType;
import org.springframework.stereotype.Service;

@Service
public class CommandInterpreter {

    public IntentResult interpret(String transcript) {
        String normalized = transcript == null ? "" : transcript.toLowerCase();
        boolean mentionsLight = normalized.contains("light") || normalized.contains("lamp");
        boolean turnOn = normalized.contains("turn on") || normalized.contains("switch on") || normalized.contains("lights on");
        boolean turnOff = normalized.contains("turn off") || normalized.contains("switch off") || normalized.contains("lights off");

        if (mentionsLight && turnOn) {
            return new IntentResult(IntentType.TURN_LIGHT_ON, "living-room-lights", ApplianceStatus.ON,
                    "Turn living room lights on");
        }
        if (mentionsLight && turnOff) {
            return new IntentResult(IntentType.TURN_LIGHT_OFF, "living-room-lights", ApplianceStatus.OFF,
                    "Turn living room lights off");
        }
        if (normalized.contains("how are you") || normalized.contains("hello") || normalized.contains("hi ")) {
            return IntentResult.general();
        }
        return IntentResult.unknown();
    }
}
