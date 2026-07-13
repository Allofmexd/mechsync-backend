package com.mechsync.modules.customers.infrastructure.repository;

import com.mechsync.modules.customers.infrastructure.persistence.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, Long> {

    boolean existsByUserId(Long userId);

    @Query(value = "SELECT COUNT(*) FROM users WHERE id_users = :userId", nativeQuery = true)
    long countUsersById(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(*) FROM vehicles WHERE customer_id = :customerId", nativeQuery = true)
    long countVehiclesByCustomerId(@Param("customerId") Long customerId);
}
