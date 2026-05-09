package com.iris.assistant.domain;

public record IntentResult(
        IntentType type,
        String applianceId,
        ApplianceStatus targetStatus,
        String summary
) {

    public static IntentResult general() {
        return new IntentResult(IntentType.GENERAL_CHAT, null, null, "General conversation");
    }

    public static IntentResult unknown() {
        return new IntentResult(IntentType.UNKNOWN, null, null, "No supported command detected");
    }
}
