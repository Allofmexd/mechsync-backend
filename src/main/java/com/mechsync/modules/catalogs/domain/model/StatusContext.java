package com.mechsync.modules.catalogs.domain.model;

import com.mechsync.modules.catalogs.domain.exception.InvalidStatusContextException;
import java.util.Locale;

public enum StatusContext {
    USERS,
    SERVICES,
    PARTS,
    VEHICLE_INTAKES,
    WORK_ORDERS,
    JOBS,
    SERVICE_REPORTS;

    public static StatusContext from(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidStatusContextException(value);
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidStatusContextException(value);
        }
    }
}
