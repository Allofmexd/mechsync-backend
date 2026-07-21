package com.mechsync.modules.users.infrastructure.persistence;

import com.mechsync.modules.users.application.dto.UserPage;
import com.mechsync.modules.users.application.port.out.UserRepositoryPort;
import com.mechsync.modules.users.domain.exception.DuplicateUserEmailException;
import com.mechsync.modules.users.domain.model.Role;
import com.mechsync.modules.users.domain.model.User;
import com.mechsync.modules.users.infrastructure.repository.RoleJpaRepository;
import com.mechsync.modules.users.infrastructure.repository.UserJpaRepository;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository userRepository;
    private final RoleJpaRepository roleRepository;

    public UserPersistenceAdapter(
            UserJpaRepository userRepository,
            RoleJpaRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserPage findAll(int page, int size) {
        Page<UserJpaEntity> result = userRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")));
        return new UserPage(
                result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public Optional<User> findById(Long userId) {
        return userRepository.findOneWithRolesById(userId).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existsByEmailExcludingId(String email, Long userId) {
        return userRepository.existsByEmailIgnoreCaseAndIdNot(email, userId);
    }

    @Override
    public boolean hasCustomerProfile(Long userId) {
        return userRepository.countCustomersByUserId(userId) > 0;
    }

    @Override
    public User save(User user) {
        Set<RoleJpaEntity> roles = user.roles().stream()
                .map(role -> roleRepository.getReferenceById(role.id()))
                .collect(Collectors.toSet());
        UserJpaEntity entity = new UserJpaEntity(
                user.id(),
                user.firstName(),
                user.lastName(),
                user.phone(),
                user.email(),
                user.passwordHash(),
                user.createdAt(),
                user.updatedAt(),
                roles);
        try {
            return toDomain(userRepository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateUserEmailException(user.email());
        }
    }

    private User toDomain(UserJpaEntity entity) {
        Set<Role> roles = entity.getRoles().stream()
                .map(role -> new Role(role.getId(), role.getName()))
                .collect(Collectors.toSet());
        return new User(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getPasswordHash(),
                roles,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
