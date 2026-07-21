package com.mechsync.modules.users.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.mechsync.modules.users.infrastructure.repository.RoleJpaRepository;
import com.mechsync.modules.users.infrastructure.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserPersistenceAdapterTest {

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private RoleJpaRepository roleRepository;

    private UserPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserPersistenceAdapter(userRepository, roleRepository);
    }

    @Test
    void detectsCustomerProfileWithoutLoadingCustomerData() {
        when(userRepository.countCustomersByUserId(7L)).thenReturn(1L);
        when(userRepository.countCustomersByUserId(8L)).thenReturn(0L);

        assertTrue(adapter.hasCustomerProfile(7L));
        assertFalse(adapter.hasCustomerProfile(8L));
    }
}
