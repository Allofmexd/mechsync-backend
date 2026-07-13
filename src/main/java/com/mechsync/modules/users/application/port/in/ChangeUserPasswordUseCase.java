package com.mechsync.modules.users.application.port.in;

import com.mechsync.modules.users.application.dto.ChangeUserPasswordCommand;

public interface ChangeUserPasswordUseCase {
    void changePassword(ChangeUserPasswordCommand command);
}
