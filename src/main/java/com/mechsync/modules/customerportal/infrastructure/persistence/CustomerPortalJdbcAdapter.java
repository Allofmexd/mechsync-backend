package com.mechsync.modules.customerportal.infrastructure.persistence;

import com.mechsync.modules.customerportal.application.port.out.CustomerPortalQueryPort;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalProfile;
import com.mechsync.modules.vehicles.application.dto.VehiclePage;
import com.mechsync.modules.vehicles.domain.model.Vehicle;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerPortalJdbcAdapter implements CustomerPortalQueryPort {

    static final String PROFILE_SQL = """
            SELECT customer.id_customers AS customer_id,
                   user.first_name,
                   user.last_name,
                   user.email,
                   user.phone,
                   customer.address
              FROM customers customer
              JOIN users user ON user.id_users = customer.user_id
             WHERE customer.id_customers = ?
            """;

    static final String VEHICLES_SQL = """
            SELECT vehicle.id_vehicles,
                   vehicle.customer_id,
                   vehicle.brand,
                   vehicle.model,
                   vehicle.year,
                   vehicle.color,
                   vehicle.license_plate,
                   vehicle.vin,
                   vehicle.current_mileage,
                   vehicle.created_at,
                   vehicle.updated_at
              FROM vehicles vehicle
             WHERE vehicle.customer_id = ?
             ORDER BY vehicle.created_at DESC, vehicle.id_vehicles DESC
             LIMIT ? OFFSET ?
            """;

    static final String VEHICLES_COUNT_SQL = """
            SELECT COUNT(*)
              FROM vehicles vehicle
             WHERE vehicle.customer_id = ?
            """;

    static final String VEHICLE_DETAIL_SQL = """
            SELECT vehicle.id_vehicles,
                   vehicle.customer_id,
                   vehicle.brand,
                   vehicle.model,
                   vehicle.year,
                   vehicle.color,
                   vehicle.license_plate,
                   vehicle.vin,
                   vehicle.current_mileage,
                   vehicle.created_at,
                   vehicle.updated_at
              FROM vehicles vehicle
             WHERE vehicle.id_vehicles = ?
               AND vehicle.customer_id = ?
            """;

    private final JdbcTemplate jdbc;

    public CustomerPortalJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<CustomerPortalProfile> findProfileByCustomerId(Long customerId) {
        List<CustomerPortalProfile> profiles = jdbc.query(
                PROFILE_SQL,
                (result, rowNumber) -> new CustomerPortalProfile(
                        result.getLong("customer_id"),
                        result.getString("first_name"),
                        result.getString("last_name"),
                        result.getString("email"),
                        result.getString("phone"),
                        result.getString("address")),
                customerId);
        return profiles.stream().findFirst();
    }

    @Override
    public VehiclePage findVehiclesByCustomerId(Long customerId, int page, int size) {
        long offset = (long) page * size;
        List<Vehicle> vehicles = jdbc.query(
                VEHICLES_SQL,
                vehicleMapper(),
                customerId,
                size,
                offset);
        Long count = jdbc.queryForObject(VEHICLES_COUNT_SQL, Long.class, customerId);
        long totalElements = count == null ? 0L : count;
        int totalPages = CustomerPortalJdbcSupport.totalPages(totalElements, size);
        return new VehiclePage(vehicles, page, size, totalElements, totalPages);
    }

    @Override
    public Optional<Vehicle> findVehicleByIdAndCustomerId(Long vehicleId, Long customerId) {
        return jdbc.query(
                        VEHICLE_DETAIL_SQL,
                        vehicleMapper(),
                        vehicleId,
                        customerId)
                .stream()
                .findFirst();
    }

    private RowMapper<Vehicle> vehicleMapper() {
        return (result, rowNumber) -> mapVehicle(result);
    }

    private Vehicle mapVehicle(ResultSet result) throws SQLException {
        return new Vehicle(
                result.getLong("id_vehicles"),
                result.getLong("customer_id"),
                result.getString("brand"),
                result.getString("model"),
                result.getInt("year"),
                result.getString("color"),
                result.getString("license_plate"),
                result.getString("vin"),
                result.getObject("current_mileage", Integer.class),
                result.getTimestamp("created_at") == null
                        ? null
                        : result.getTimestamp("created_at").toLocalDateTime(),
                result.getTimestamp("updated_at") == null
                        ? null
                        : result.getTimestamp("updated_at").toLocalDateTime());
    }
}
