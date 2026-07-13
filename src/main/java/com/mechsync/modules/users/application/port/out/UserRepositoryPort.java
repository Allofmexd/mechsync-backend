package com.mechsync.modules.users.application.port.out;

import com.mechsync.modules.users.application.dto.UserPage;
import com.mechsync.modules.users.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {
    UserPage findAll(int page, int size);
    Optional<User> findById(Long userId);
    boolean existsByEmail(String email);
    boolean existsByEmailExcludingId(String email, Long userId);
    User save(User user);
}
