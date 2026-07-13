package com.mechsync.shared.web.controller;

import com.mechsync.shared.application.health.DatabaseHealthChecker;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private static final String APPLICATION_NAME = "mechsync-backend";

    private final DatabaseHealthChecker databaseHealthChecker;

    public HealthController(DatabaseHealthChecker databaseHealthChecker) {
        this.databaseHealthChecker = databaseHealthChecker;
    }

    @GetMapping(ApiPaths.HEALTH)
    public ApiResponse<HealthData> check() {
        return ApiResponse.ok(new HealthData("UP", APPLICATION_NAME, Instant.now()));
    }

    @GetMapping(ApiPaths.HEALTH_DATABASE)
    public ResponseEntity<ApiResponse<DatabaseHealthData>> checkDatabase() {
        Instant checkedAt = Instant.now();

        if (databaseHealthChecker.isAvailable()) {
            return ResponseEntity.ok(
                    ApiResponse.ok(new DatabaseHealthData("UP", checkedAt)));
        }

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(new DatabaseHealthData("DOWN", checkedAt)));
    }

    public record HealthData(String status, String application, Instant timestamp) {
    }

    public record DatabaseHealthData(String status, Instant timestamp) {
    }
}
