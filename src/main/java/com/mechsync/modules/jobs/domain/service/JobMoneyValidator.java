package com.mechsync.modules.jobs.domain.service;

import com.mechsync.modules.jobs.domain.exception.InvalidJobException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class JobMoneyValidator {
    private static final int MONEY_SCALE = 2;

    public Amounts validate(BigDecimal subtotal, BigDecimal iva, BigDecimal total) {
        if (subtotal == null || iva == null || total == null) {
            throw new InvalidJobException("Real amounts are required to complete a Job");
        }
        BigDecimal normalizedSubtotal = normalize(subtotal);
        BigDecimal normalizedIva = normalize(iva);
        BigDecimal normalizedTotal = normalize(total);
        if (normalizedSubtotal.signum() < 0 || normalizedIva.signum() < 0
                || normalizedTotal.signum() < 0) {
            throw new InvalidJobException("Real amounts must be non-negative");
        }
        BigDecimal expected = normalizedSubtotal.add(normalizedIva).setScale(MONEY_SCALE);
        if (expected.compareTo(normalizedTotal) != 0) {
            throw new InvalidJobException("realTotalAmount must equal realSubtotalAmount plus realIvaAmount");
        }
        return new Amounts(normalizedSubtotal, normalizedIva, normalizedTotal);
    }

    private BigDecimal normalize(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public record Amounts(BigDecimal subtotal, BigDecimal iva, BigDecimal total) {
    }
}
