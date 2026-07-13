package com.mechsync.modules.users.application.port.in;

import com.mechsync.modules.users.application.dto.UpdateUserCommand;
import com.mechsync.modules.users.domain.model.User;

public interface UpdateUserUseCase {
    User update(UpdateUserCommand command);
}
