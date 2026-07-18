package com.mechsync.modules.workorders.web.controller;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record ApproveWorkOrderRevisionRequest(
        @NotBlank @Size(max = 200) String acceptedByName,
        @Positive Long acceptedByUserId,
        LocalDateTime acceptedAt,
        @NotBlank @Size(max = 30) String acceptanceMethod,
        @Pattern(regexp = ".*\\S.*", message = "must not be blank") String acceptanceNotes) {
}
