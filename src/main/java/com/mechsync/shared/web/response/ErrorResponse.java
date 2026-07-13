package com.mechsync.shared.web.response;

import java.time.Instant;

public record ErrorResponse(String message, Instant timestamp) {
}
