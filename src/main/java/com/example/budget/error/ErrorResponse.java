package com.example.budget.error;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, int status, String error) {}
