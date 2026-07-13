package com.mechsync.modules.users.application.port.in;

import com.mechsync.modules.users.domain.model.User;

public interface GetUserByIdUseCase {
    User getById(Long userId);
}
