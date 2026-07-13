package com.mechsync.modules.auth.application.port.out;

import com.mechsync.modules.auth.application.dto.GeneratedToken;
import com.mechsync.modules.auth.domain.model.AuthenticatedUser;

public interface TokenGeneratorPort {

    GeneratedToken generate(AuthenticatedUser user);
}
