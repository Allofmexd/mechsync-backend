package com.mechsync.modules.customerportal.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.mechsync.modules.customerportal.domain.model.CustomerPortalProfile;
import com.mechsync.modules.vehicles.application.dto.VehiclePage;
import com.mechsync.modules.vehicles.domain.model.Vehicle;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class CustomerPortalJdbcAdapterTest {

    private JdbcTemplate jdbc;
    private CustomerPortalJdbcAdapter adapter;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        adapter = new CustomerPortalJdbcAdapter(jdbc);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void profileQueryUsesResolvedCustomerId() {
        CustomerPortalProfile profile = new CustomerPortalProfile(
                3L, "Ana", "Cliente", "ana@example.com", null, null);
        doReturn(List.of(profile)).when(jdbc).query(
                eq(CustomerPortalJdbcAdapter.PROFILE_SQL), any(RowMapper.class), eq(3L));

        assertEquals(profile, adapter.findProfileByCustomerId(3L).orElseThrow());
        verify(jdbc).query(
                eq(CustomerPortalJdbcAdapter.PROFILE_SQL), any(RowMapper.class), eq(3L));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void pagedVehicleQueryAndCountUseSameCustomerOwnership() {
        Vehicle vehicle = vehicle();
        doReturn(List.of(vehicle)).when(jdbc).query(
                eq(CustomerPortalJdbcAdapter.VEHICLES_SQL), any(RowMapper.class),
                eq(3L), eq(5), eq(5L));
        doReturn(6L).when(jdbc).queryForObject(
                CustomerPortalJdbcAdapter.VEHICLES_COUNT_SQL, Long.class, 3L);

        VehiclePage result = adapter.findVehiclesByCustomerId(3L, 1, 5);

        assertEquals(List.of(vehicle), result.content());
        assertEquals(6, result.totalElements());
        assertEquals(2, result.totalPages());
        verify(jdbc).queryForObject(
                CustomerPortalJdbcAdapter.VEHICLES_COUNT_SQL, Long.class, 3L);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void vehicleDetailUsesVehicleAndCustomerInOneQuery() {
        Vehicle vehicle = vehicle();
        doReturn(List.of(vehicle)).when(jdbc).query(
                eq(CustomerPortalJdbcAdapter.VEHICLE_DETAIL_SQL), any(RowMapper.class),
                eq(8L), eq(3L));

        assertEquals(vehicle, adapter.findVehicleByIdAndCustomerId(8L, 3L).orElseThrow());
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void missingOwnedVehicleReturnsEmpty() {
        doReturn(List.of()).when(jdbc).query(
                eq(CustomerPortalJdbcAdapter.VEHICLE_DETAIL_SQL), any(RowMapper.class),
                eq(99L), eq(3L));

        assertTrue(adapter.findVehicleByIdAndCustomerId(99L, 3L).isEmpty());
    }

    @Test
    void sqlKeepsOwnershipAndStableOrderInsideDatabase() {
        assertAll(
                () -> assertTrue(CustomerPortalJdbcAdapter.PROFILE_SQL.contains(
                        "customer.id_customers = ?")),
                () -> assertTrue(CustomerPortalJdbcAdapter.VEHICLES_SQL.contains(
                        "vehicle.customer_id = ?")),
                () -> assertTrue(CustomerPortalJdbcAdapter.VEHICLES_COUNT_SQL.contains(
                        "vehicle.customer_id = ?")),
                () -> assertTrue(CustomerPortalJdbcAdapter.VEHICLE_DETAIL_SQL.contains(
                        "vehicle.id_vehicles = ?")),
                () -> assertTrue(CustomerPortalJdbcAdapter.VEHICLE_DETAIL_SQL.contains(
                        "vehicle.customer_id = ?")),
                () -> assertTrue(CustomerPortalJdbcAdapter.VEHICLES_SQL.contains(
                        "ORDER BY vehicle.created_at DESC, vehicle.id_vehicles DESC")),
                () -> assertTrue(CustomerPortalJdbcAdapter.VEHICLES_SQL.contains("LIMIT ? OFFSET ?")));
    }

    private Vehicle vehicle() {
        return new Vehicle(
                8L, 3L, "Honda", "Accord", 2022, "Rojo", "ABC-123",
                "1HGCM82633A001234", 45000, LocalDateTime.of(2026, 7, 21, 12, 0), null);
    }
}
