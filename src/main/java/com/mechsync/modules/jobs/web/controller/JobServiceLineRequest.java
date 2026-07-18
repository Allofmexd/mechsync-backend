package com.mechsync.modules.jobs.web.controller;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record JobServiceLineRequest(
        @NotNull @Positive Long serviceId,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 8, fraction = 2)
        BigDecimal quantity,
        @NotNull @DecimalMin(value = "0.00") @Digits(integer = 8, fraction = 2)
        BigDecimal unitPrice) {
}
