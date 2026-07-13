package com.mechsync.modules.auth.application.port.out;

import com.mechsync.modules.auth.domain.model.UserCredentials;
import java.util.Optional;

public interface LoadUserCredentialsPort {

    Optional<UserCredentials> loadByEmail(String email);
}
