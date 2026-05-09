package com.iris.assistant.domain;

import java.util.Set;

public record UserProfile(
        String faceId,
        String displayName,
        String relationship,
        Set<String> applianceAccess
) {

    public boolean canControl(String applianceId) {
        return applianceAccess.contains(applianceId);
    }
}
