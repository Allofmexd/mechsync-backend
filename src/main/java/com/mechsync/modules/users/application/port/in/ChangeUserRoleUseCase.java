package com.mechsync.modules.users.application.port.in;

import com.mechsync.modules.users.application.dto.ChangeUserRoleCommand;
import com.mechsync.modules.users.domain.model.User;

public interface ChangeUserRoleUseCase {
    User changeRole(ChangeUserRoleCommand command);
}
