package com.mechsync.modules.technicians.infrastructure.repository;

import com.mechsync.modules.technicians.infrastructure.persistence.TechnicianJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface TechnicianJpaRepository extends Repository<TechnicianJpaEntity, Long> {

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
                t.hire_date AS hireDate
            FROM technicians t
            INNER JOIN users u ON u.id_users = t.user_id
            INNER JOIN specialties s ON s.id_specialties = t.specialty_id
            ORDER BY u.first_name, u.last_name, t.id_technicians
            """, nativeQuery = true)
    List<TechnicianDetailsProjection> findAllDetails();
}
