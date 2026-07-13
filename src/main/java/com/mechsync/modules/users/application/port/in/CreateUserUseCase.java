package com.mechsync.modules.users.application.port.in;

import com.mechsync.modules.users.application.dto.CreateUserCommand;
import com.mechsync.modules.users.domain.model.User;

public interface CreateUserUseCase {
    User create(CreateUserCommand command);
}
