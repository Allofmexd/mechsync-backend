package com.mechsync;

import com.mechsync.shared.application.health.DatabaseHealthChecker;
import com.mechsync.modules.catalogs.infrastructure.repository.CatalogStatusJpaRepository;
import com.mechsync.modules.customers.infrastructure.repository.CustomerJpaRepository;
import com.mechsync.modules.users.infrastructure.repository.RoleJpaRepository;
import com.mechsync.modules.users.infrastructure.repository.UserJpaRepository;
import com.mechsync.modules.vehicles.infrastructure.repository.VehicleJpaRepository;
import com.mechsync.modules.vehicleintakes.infrastructure.repository.VehicleIntakeJpaRepository;
import com.mechsync.modules.workorders.infrastructure.repository.WorkOrderJpaRepository;
import com.mechsync.modules.technicians.infrastructure.repository.TechnicianJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "debug=false",
        "mechsync.security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "mechsync.security.jwt.expiration-minutes=120",
        "mechsync.security.jwt.issuer=mechsync-backend",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration"
})
class MechSyncApplicationTests {

    @MockitoBean
    private DatabaseHealthChecker databaseHealthChecker;

    @MockitoBean
    private UserJpaRepository userJpaRepository;

    @MockitoBean
    private CustomerJpaRepository customerJpaRepository;

    @MockitoBean
    private RoleJpaRepository roleJpaRepository;

    @MockitoBean
    private VehicleJpaRepository vehicleJpaRepository;

    @MockitoBean
    private VehicleIntakeJpaRepository vehicleIntakeJpaRepository;

    @MockitoBean
    private WorkOrderJpaRepository workOrderJpaRepository;

    @MockitoBean
    private CatalogStatusJpaRepository catalogStatusJpaRepository;

    @MockitoBean
    private TechnicianJpaRepository technicianJpaRepository;

    @Test
    void contextLoads() {
    }
}
