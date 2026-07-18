package com.mechsync.modules.jobs.web.controller;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CompleteJobRequest(
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2)
        BigDecimal realSubtotalAmount,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2)
        BigDecimal realIvaAmount,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2)
        BigDecimal realTotalAmount,
        String notes) {
}
