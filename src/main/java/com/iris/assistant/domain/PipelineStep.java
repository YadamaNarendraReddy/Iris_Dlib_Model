package com.iris.assistant.domain;

public record PipelineStep(
        String id,
        String label,
        String state,
        String detail
) {
}
