package com.mechsync.modules.auth.application.usecase;

import com.mechsync.modules.auth.application.dto.GeneratedToken;
import com.mechsync.modules.auth.application.dto.LoginCommand;
import com.mechsync.modules.auth.application.dto.LoginResult;
import com.mechsync.modules.auth.application.port.in.LoginUseCase;
import com.mechsync.modules.auth.application.port.out.LoadUserCredentialsPort;
import com.mechsync.modules.auth.application.port.out.PasswordVerifierPort;
import com.mechsync.modules.auth.application.port.out.TokenGeneratorPort;
import com.mechsync.modules.auth.domain.exception.InvalidCredentialsException;
import com.mechsync.modules.auth.domain.model.UserCredentials;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements LoginUseCase {

    private final LoadUserCredentialsPort loadUserCredentialsPort;
    private final PasswordVerifierPort passwordVerifierPort;
    private final TokenGeneratorPort tokenGeneratorPort;

    public LoginService(
            LoadUserCredentialsPort loadUserCredentialsPort,
            PasswordVerifierPort passwordVerifierPort,
            TokenGeneratorPort tokenGeneratorPort) {
        this.loadUserCredentialsPort = loadUserCredentialsPort;
        this.passwordVerifierPort = passwordVerifierPort;
        this.tokenGeneratorPort = tokenGeneratorPort;
    }

    @Override
    public LoginResult login(LoginCommand command) {
        String normalizedEmail = command.email().trim().toLowerCase(Locale.ROOT);
        UserCredentials credentials = loadUserCredentialsPort.loadByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordVerifierPort.matches(command.password(), credentials.passwordHash())) {
            throw new InvalidCredentialsException();
        }

        GeneratedToken token = tokenGeneratorPort.generate(credentials.user());
        return new LoginResult(token, credentials.user());
    }
}
