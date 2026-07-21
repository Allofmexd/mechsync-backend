package com.mechsync.modules.customerportal.infrastructure.persistence;

import com.mechsync.modules.customerportal.domain.model.CustomerPortalPage;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalVehicleInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

final class CustomerPortalJdbcSupport {
    private CustomerPortalJdbcSupport() {
    }

    static int totalPages(long totalElements, int size) {
        return totalElements == 0 ? 0 : (int) ((totalElements + size - 1) / size);
    }

    static <T> CustomerPortalPage<T> page(
            List<T> content, int page, int size, Long count) {
        long total = count == null ? 0L : count;
        return new CustomerPortalPage<>(content, page, size, total, totalPages(total, size));
    }

    static CustomerPortalVehicleInfo vehicle(ResultSet result) throws SQLException {
        String brand = result.getString("brand");
        String model = result.getString("model");
        int year = result.getInt("year");
        return new CustomerPortalVehicleInfo(
                result.getLong("vehicle_id"),
                brand,
                model,
                year,
                result.getString("license_plate"),
                brand + " " + model + " " + year);
    }

    static LocalDateTime dateTime(ResultSet result, String column) throws SQLException {
        Timestamp value = result.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }
}
