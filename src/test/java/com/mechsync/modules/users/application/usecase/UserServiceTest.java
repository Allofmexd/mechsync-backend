package com.mechsync.modules.users.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mechsync.modules.users.application.dto.ChangeUserPasswordCommand;
import com.mechsync.modules.users.application.dto.ChangeUserRoleCommand;
import com.mechsync.modules.users.application.dto.CreateUserCommand;
import com.mechsync.modules.users.application.dto.UpdateUserCommand;
import com.mechsync.modules.users.application.port.out.PasswordHasherPort;
import com.mechsync.modules.users.application.port.out.RoleRepositoryPort;
import com.mechsync.modules.users.application.port.out.UserRepositoryPort;
import com.mechsync.modules.users.domain.exception.DuplicateUserEmailException;
import com.mechsync.modules.users.domain.exception.RoleNotFoundException;
import com.mechsync.modules.users.domain.exception.SelfRoleChangeNotAllowedException;
import com.mechsync.modules.users.domain.exception.UserNotFoundException;
import com.mechsync.modules.users.domain.model.Role;
import com.mechsync.modules.users.domain.model.User;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private RoleRepositoryPort roleRepository;
    @Mock private PasswordHasherPort passwordHasher;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, roleRepository, passwordHasher);
    }

    @Test
    void createsUserWithHashAndSingleExistingRole() {
        Role customerRole = new Role(3L, "CLIENTE");
        when(userRepository.existsByEmail("juan@example.com")).thenReturn(false);
        when(roleRepository.findByName("CLIENTE")).thenReturn(Optional.of(customerRole));
        when(passwordHasher.hash("Password123!")).thenReturn("bcrypt-hash");
        when(userRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return new User(2L, user.firstName(), user.lastName(), user.phone(), user.email(),
                    user.passwordHash(), user.roles(), LocalDateTime.now(), null);
        });

        User result = service.create(new CreateUserCommand(
                " Juan ", " Pérez ", " 9610000000 ", " JUAN@EXAMPLE.COM ",
                "Password123!", "CLIENTE"));

        assertEquals("juan@example.com", result.email());
        assertEquals("bcrypt-hash", result.passwordHash());
        assertEquals(Set.of(customerRole), result.roles());
        assertEquals("Juan", result.firstName());
    }

    @Test
    void duplicateEmailIsRejectedBeforeHashing() {
        when(userRepository.existsByEmail("admin@mechsync.local")).thenReturn(true);

        assertThrows(DuplicateUserEmailException.class, () -> service.create(new CreateUserCommand(
                "Admin", "Local", null, "admin@mechsync.local", "Password123!", "ADMINISTRADOR")));
        verify(passwordHasher, never()).hash(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void missingSeededRoleIsRejected() {
        when(userRepository.existsByEmail("tech@example.com")).thenReturn(false);
        when(roleRepository.findByName("TECNICO")).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> service.create(new CreateUserCommand(
                "Tech", "User", null, "tech@example.com", "Password123!", "TECNICO")));
    }

    @Test
    void missingUserThrowsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void updatesBasicFieldsWithoutChangingHashOrRole() {
        User current = user();
        when(userRepository.findById(1L)).thenReturn(Optional.of(current));
        when(userRepository.existsByEmailExcludingId("new@example.com", 1L)).thenReturn(false);
        when(userRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.update(new UpdateUserCommand(
                1L, " New ", " Name ", null, " NEW@EXAMPLE.COM "));

        assertEquals("new@example.com", result.email());
        assertEquals(current.passwordHash(), result.passwordHash());
        assertEquals(current.roles(), result.roles());
    }

    @Test
    void passwordResetStoresOnlyNewHash() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(passwordHasher.hash("NewPassword123!")).thenReturn("new-bcrypt-hash");
        when(userRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.changePassword(new ChangeUserPasswordCommand(1L, "NewPassword123!"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("new-bcrypt-hash", captor.getValue().passwordHash());
    }

    @Test
    void administratorCannotChangeOwnRole() {
        assertThrows(SelfRoleChangeNotAllowedException.class,
                () -> service.changeRole(new ChangeUserRoleCommand(1L, 1L, "CLIENTE")));
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void roleChangeReplacesRolesWithExactlyOne() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(userWithId(2L)));
        Role technician = new Role(2L, "TECNICO");
        when(roleRepository.findByName("TECNICO")).thenReturn(Optional.of(technician));
        when(userRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.changeRole(new ChangeUserRoleCommand(2L, 1L, "TECNICO"));

        assertEquals(Set.of(technician), result.roles());
        assertTrue(result.updatedAt() != null);
    }

    private User user() {
        return userWithId(1L);
    }

    private User userWithId(Long id) {
        return new User(id, "Admin", "Local", null, "admin@mechsync.local",
                "existing-hash", Set.of(new Role(1L, "ADMINISTRADOR")),
                LocalDateTime.of(2026, 1, 1, 12, 0), null);
    }
}
