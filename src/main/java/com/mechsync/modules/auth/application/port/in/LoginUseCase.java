package com.mechsync.modules.auth.application.port.in;

import com.mechsync.modules.auth.application.dto.LoginCommand;
import com.mechsync.modules.auth.application.dto.LoginResult;

public interface LoginUseCase {

    LoginResult login(LoginCommand command);
}
