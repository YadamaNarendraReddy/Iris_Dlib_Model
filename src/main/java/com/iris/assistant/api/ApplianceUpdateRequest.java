package com.iris.assistant.api;

import com.iris.assistant.domain.ApplianceStatus;

public record ApplianceUpdateRequest(ApplianceStatus status) {
}
