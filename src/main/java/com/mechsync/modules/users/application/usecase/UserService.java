package com.mechsync.modules.users.application.usecase;

import com.mechsync.modules.users.application.dto.ChangeUserPasswordCommand;
import com.mechsync.modules.users.application.dto.ChangeUserRoleCommand;
import com.mechsync.modules.users.application.dto.CreateUserCommand;
import com.mechsync.modules.users.application.dto.UpdateUserCommand;
import com.mechsync.modules.users.application.dto.UserPage;
import com.mechsync.modules.users.application.port.in.ChangeUserPasswordUseCase;
import com.mechsync.modules.users.application.port.in.ChangeUserRoleUseCase;
import com.mechsync.modules.users.application.port.in.CreateUserUseCase;
import com.mechsync.modules.users.application.port.in.GetUserByIdUseCase;
import com.mechsync.modules.users.application.port.in.ListUsersUseCase;
import com.mechsync.modules.users.application.port.in.UpdateUserUseCase;
import com.mechsync.modules.users.application.port.out.PasswordHasherPort;
import com.mechsync.modules.users.application.port.out.RoleRepositoryPort;
import com.mechsync.modules.users.application.port.out.UserRepositoryPort;
import com.mechsync.modules.users.domain.exception.DuplicateUserEmailException;
import com.mechsync.modules.users.domain.exception.InvalidUserRoleException;
import com.mechsync.modules.users.domain.exception.RoleNotFoundException;
import com.mechsync.modules.users.domain.exception.SelfRoleChangeNotAllowedException;
import com.mechsync.modules.users.domain.exception.UserNotFoundException;
import com.mechsync.modules.users.domain.exception.UserCustomerRoleConflictException;
import com.mechsync.modules.users.domain.model.Role;
import com.mechsync.modules.users.domain.model.User;
import com.mechsync.shared.domain.constant.SystemRole;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService implements
        ListUsersUseCase,
        GetUserByIdUseCase,
        CreateUserUseCase,
        UpdateUserUseCase,
        ChangeUserPasswordUseCase,
        ChangeUserRoleUseCase {

    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;
    private final PasswordHasherPort passwordHasher;

    public UserService(
            UserRepositoryPort userRepository,
            RoleRepositoryPort roleRepository,
            PasswordHasherPort passwordHasher) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public UserPage list(int page, int size) {
        return userRepository.findAll(page, size);
    }

    @Override
    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional
    public User create(CreateUserCommand command) {
        String email = normalizeEmail(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserEmailException(email);
        }
        Role role = loadRole(command.role());
        User user = new User(
                null,
                command.firstName().trim(),
                command.lastName().trim(),
                normalizeOptional(command.phone()),
                email,
                passwordHasher.hash(command.password()),
                Set.of(role),
                null,
                null);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User update(UpdateUserCommand command) {
        User current = getById(command.userId());
        String email = normalizeEmail(command.email());
        if (userRepository.existsByEmailExcludingId(email, current.id())) {
            throw new DuplicateUserEmailException(email);
        }
        return userRepository.save(new User(
                current.id(),
                command.firstName().trim(),
                command.lastName().trim(),
                normalizeOptional(command.phone()),
                email,
                current.passwordHash(),
                current.roles(),
                current.createdAt(),
                LocalDateTime.now()));
    }

    @Override
    @Transactional
    public void changePassword(ChangeUserPasswordCommand command) {
        User current = getById(command.userId());
        userRepository.save(new User(
                current.id(),
                current.firstName(),
                current.lastName(),
                current.phone(),
                current.email(),
                passwordHasher.hash(command.newPassword()),
                current.roles(),
                current.createdAt(),
                LocalDateTime.now()));
    }

    @Override
    @Transactional
    public User changeRole(ChangeUserRoleCommand command) {
        if (command.userId().equals(command.administratorId())) {
            throw new SelfRoleChangeNotAllowedException();
        }
        User current = getById(command.userId());
        Role role = loadRole(command.role());
        if (!SystemRole.CLIENTE.name().equals(role.name())
                && userRepository.hasCustomerProfile(current.id())) {
            throw new UserCustomerRoleConflictException(current.id());
        }
        return userRepository.save(new User(
                current.id(),
                current.firstName(),
                current.lastName(),
                current.phone(),
                current.email(),
                current.passwordHash(),
                Set.of(role),
                current.createdAt(),
                LocalDateTime.now()));
    }

    private Role loadRole(String requestedRole) {
        String roleName;
        try {
            roleName = SystemRole.valueOf(requestedRole).name();
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new InvalidUserRoleException(requestedRole);
        }
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException(roleName));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        return value == null ? null : value.trim();
    }
}
