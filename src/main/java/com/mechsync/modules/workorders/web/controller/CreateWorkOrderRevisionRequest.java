package com.mechsync.modules.workorders.web.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CreateWorkOrderRevisionRequest(
        @NotNull @Positive Long technicianId,
        LocalDateTime estimatedStartDate,
        LocalDateTime estimatedDeliveryDate,
        @PositiveOrZero @Digits(integer = 6, fraction = 4) BigDecimal estimatedHours,
        @Pattern(regexp = "[A-Za-z]{3}") String currency,
        boolean applyIva,
        @PositiveOrZero @Digits(integer = 4, fraction = 6) BigDecimal ivaRate,
        @NotNull @PositiveOrZero @Digits(integer = 17, fraction = 2) BigDecimal subtotalAmount,
        @PositiveOrZero @Digits(integer = 17, fraction = 2) BigDecimal ivaAmount,
        @PositiveOrZero @Digits(integer = 17, fraction = 2) BigDecimal totalAmount,
        @Pattern(regexp = ".*\\S.*", message = "must not be blank") String taxTreatmentNotes,
        @Pattern(regexp = ".*\\S.*", message = "must not be blank") String technicalObservations,
        @Pattern(regexp = ".*\\S.*", message = "must not be blank") String customerNotes,
        @Size(max = 500) @Pattern(regexp = ".*\\S.*", message = "must not be blank") String changeReason,
        List<@Valid ServiceLine> services,
        List<@Valid PartLine> parts) {

    public record ServiceLine(
            @Positive Integer lineNumber,
            @Positive Long serviceId,
            @Size(max = 150) @Pattern(regexp = ".*\\S.*", message = "must not be blank") String nameSnapshot,
            String descriptionSnapshot,
            @NotNull @Positive @Digits(integer = 13, fraction = 6) BigDecimal quantity,
            @NotNull @PositiveOrZero @Digits(integer = 15, fraction = 4) BigDecimal unitPrice,
            @PositiveOrZero @Digits(integer = 15, fraction = 4) BigDecimal lineSubtotal,
            @Pattern(regexp = ".*\\S.*", message = "must not be blank") String notes) {
    }

    public record PartLine(
            @Positive Integer lineNumber,
            @Positive Long partId,
            @Size(max = 150) @Pattern(regexp = ".*\\S.*", message = "must not be blank") String nameSnapshot,
            String descriptionSnapshot,
            @NotNull @Positive @Digits(integer = 13, fraction = 6) BigDecimal quantity,
            @NotNull @PositiveOrZero @Digits(integer = 15, fraction = 4) BigDecimal unitPrice,
            @PositiveOrZero @Digits(integer = 15, fraction = 4) BigDecimal lineSubtotal,
            @Pattern(regexp = ".*\\S.*", message = "must not be blank") String notes) {
    }
}
