package com.mechsync.modules.auth.infrastructure.persistence;

import com.mechsync.modules.auth.application.port.out.LoadUserCredentialsPort;
import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.domain.model.UserCredentials;
import com.mechsync.modules.users.infrastructure.persistence.RoleJpaEntity;
import com.mechsync.modules.users.infrastructure.persistence.UserJpaEntity;
import com.mechsync.modules.users.infrastructure.repository.UserJpaRepository;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserAuthPersistenceAdapter implements LoadUserCredentialsPort {

    private final UserJpaRepository userJpaRepository;

    public UserAuthPersistenceAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public Optional<UserCredentials> loadByEmail(String email) {
        return userJpaRepository.findByEmailIgnoreCase(email).map(this::toCredentials);
    }

    private UserCredentials toCredentials(UserJpaEntity entity) {
        Set<String> roles = entity.getRoles().stream()
                .map(RoleJpaEntity::getName)
                .collect(Collectors.toSet());
        AuthenticatedUser user = new AuthenticatedUser(entity.getId(), entity.getEmail(), roles);
        return new UserCredentials(user, entity.getPasswordHash());
    }
}
