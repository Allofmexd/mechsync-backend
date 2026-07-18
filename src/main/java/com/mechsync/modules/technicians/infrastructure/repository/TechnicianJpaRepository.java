package com.mechsync.modules.technicians.infrastructure.repository;

import com.mechsync.modules.technicians.infrastructure.persistence.TechnicianJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TechnicianJpaRepository extends JpaRepository<TechnicianJpaEntity, Long> {

    @Query(value = """
            SELECT
                t.id_technicians AS id,
                t.user_id AS userId,
                u.first_name AS firstName,
                u.last_name AS lastName,
                u.email AS email,
                u.phone AS phone,
                s.id_specialties AS specialtyId,
                s.name AS specialtyCode,
                t.hire_date AS hireDate,
                t.created_at AS createdAt,
                t.updated_at AS updatedAt
            FROM technicians t
            INNER JOIN users u ON u.id_users = t.user_id
            INNER JOIN specialties s ON s.id_specialties = t.specialty_id
            ORDER BY u.first_name, u.last_name, t.id_technicians
            """, nativeQuery = true)
    List<TechnicianDetailsProjection> findAllDetails();

    @Query(value = """
            SELECT
                t.id_technicians AS id,
                t.user_id AS userId,
                u.first_name AS firstName,
                u.last_name AS lastName,
                u.email AS email,
                u.phone AS phone,
                s.id_specialties AS specialtyId,
                s.name AS specialtyCode,
                t.hire_date AS hireDate,
                t.created_at AS createdAt,
                t.updated_at AS updatedAt
            FROM technicians t
            INNER JOIN users u ON u.id_users = t.user_id
            INNER JOIN specialties s ON s.id_specialties = t.specialty_id
            WHERE t.id_technicians = :technicianId
            """, nativeQuery = true)
    Optional<TechnicianDetailsProjection> findDetailsById(
            @Param("technicianId") Long technicianId);

    boolean existsByUserId(Long userId);

    @Query(value = "SELECT COUNT(*) FROM users WHERE id_users = :userId", nativeQuery = true)
    long countUsersById(@Param("userId") Long userId);

    @Query(value = """
            SELECT COUNT(*)
            FROM user_roles ur
            INNER JOIN roles r ON r.id_roles = ur.role_id
            WHERE ur.user_id = :userId AND r.name = :roleName
            """, nativeQuery = true)
    long countUserRoles(
            @Param("userId") Long userId,
            @Param("roleName") String roleName);

    @Query(value = "SELECT COUNT(*) FROM specialties WHERE id_specialties = :specialtyId",
            nativeQuery = true)
    long countSpecialtiesById(@Param("specialtyId") Long specialtyId);
}
