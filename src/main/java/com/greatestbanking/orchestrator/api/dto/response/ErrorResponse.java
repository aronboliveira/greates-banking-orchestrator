package com.greatestbanking.orchestrator.api.dto.response;

public record ErrorResponse(
    int status,
    String error,
    String message
) {}
