package com.mechsync.modules.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mechsync.modules.auth.application.dto.GeneratedToken;
import com.mechsync.modules.auth.application.dto.LoginCommand;
import com.mechsync.modules.auth.application.dto.LoginResult;
import com.mechsync.modules.auth.application.port.out.LoadUserCredentialsPort;
import com.mechsync.modules.auth.application.port.out.PasswordVerifierPort;
import com.mechsync.modules.auth.application.port.out.TokenGeneratorPort;
import com.mechsync.modules.auth.domain.exception.InvalidCredentialsException;
import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.domain.model.UserCredentials;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private LoadUserCredentialsPort loadUserCredentialsPort;

    @Mock
    private PasswordVerifierPort passwordVerifierPort;

    @Mock
    private TokenGeneratorPort tokenGeneratorPort;

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginService(
                loadUserCredentialsPort, passwordVerifierPort, tokenGeneratorPort);
    }

    @Test
    void returnsTokenForValidCredentials() {
        AuthenticatedUser user = new AuthenticatedUser(
                1L, "admin@example.com", Set.of("ADMINISTRADOR"));
        UserCredentials credentials = new UserCredentials(user, "bcrypt-hash");
        when(loadUserCredentialsPort.loadByEmail("admin@example.com"))
                .thenReturn(Optional.of(credentials));
        when(passwordVerifierPort.matches("local-password", "bcrypt-hash")).thenReturn(true);
        when(tokenGeneratorPort.generate(user)).thenReturn(new GeneratedToken("jwt", 7200));

        LoginResult result = loginService.login(
                new LoginCommand(" ADMIN@example.com ", "local-password"));

        assertEquals("jwt", result.token().value());
        assertEquals(user, result.user());
    }

    @Test
    void rejectsInvalidPassword() {
        AuthenticatedUser user = new AuthenticatedUser(
                1L, "admin@example.com", Set.of("ADMINISTRADOR"));
        UserCredentials credentials = new UserCredentials(user, "bcrypt-hash");
        when(loadUserCredentialsPort.loadByEmail("admin@example.com"))
                .thenReturn(Optional.of(credentials));
        when(passwordVerifierPort.matches("wrong-password", "bcrypt-hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> loginService.login(
                new LoginCommand("admin@example.com", "wrong-password")));
        verify(tokenGeneratorPort, never()).generate(user);
    }
}
