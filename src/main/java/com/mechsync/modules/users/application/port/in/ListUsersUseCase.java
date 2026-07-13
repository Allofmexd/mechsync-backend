package com.mechsync.modules.users.application.port.in;

import com.mechsync.modules.users.application.dto.UserPage;

public interface ListUsersUseCase {
    UserPage list(int page, int size);
}
