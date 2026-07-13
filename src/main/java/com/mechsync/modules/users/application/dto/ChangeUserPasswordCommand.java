package com.mechsync.modules.users.application.dto;

public record ChangeUserPasswordCommand(Long userId, String newPassword) {
}
