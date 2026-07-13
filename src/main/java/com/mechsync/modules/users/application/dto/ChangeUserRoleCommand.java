package com.mechsync.modules.users.application.dto;

public record ChangeUserRoleCommand(Long userId, Long administratorId, String role) {
}
