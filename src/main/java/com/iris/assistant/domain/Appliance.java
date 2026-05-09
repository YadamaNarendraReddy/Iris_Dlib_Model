package com.iris.assistant.domain;

public record Appliance(
        String id,
        String displayName,
        String room,
        ApplianceStatus status
) {

    public Appliance withStatus(ApplianceStatus nextStatus) {
        return new Appliance(id, displayName, room, nextStatus);
    }
}
