package com.mechsync.modules.workorders.domain.service;

import com.mechsync.modules.workorders.domain.exception.InvalidWorkOrderRevisionException;
import com.mechsync.modules.workorders.domain.model.QuotationAmounts;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

public final class WorkOrderRevisionMoneyCalculator {

    public static final BigDecimal DEFAULT_IVA_RATE = new BigDecimal("0.160000");
    private static final BigDecimal ZERO_RATE = new BigDecimal("0.000000");
    private static final BigDecimal ZERO_MONEY = new BigDecimal("0.00");

    public QuotationAmounts calculateFromLines(
            Collection<BigDecimal> lineSubtotals,
            BigDecimal requestedSubtotal,
            boolean applyIva,
            BigDecimal requestedRate,
            BigDecimal requestedIva,
            BigDecimal requestedTotal) {
        BigDecimal calculatedSubtotal = lineSubtotals.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        validateRequested("subtotalAmount", requestedSubtotal, calculatedSubtotal);
        return calculate(
                calculatedSubtotal,
                applyIva,
                requestedRate,
                requestedIva,
                requestedTotal);
    }

    public QuotationAmounts calculate(
            BigDecimal subtotal,
            boolean applyIva,
            BigDecimal requestedRate,
            BigDecimal requestedIva,
            BigDecimal requestedTotal) {
        if (subtotal == null || subtotal.signum() < 0 || subtotal.scale() > 2) {
            throw new InvalidWorkOrderRevisionException(
                    "subtotalAmount must be non-negative and have at most 2 decimal places");
        }
        BigDecimal normalizedSubtotal = subtotal.setScale(2, RoundingMode.UNNECESSARY);
        BigDecimal rate;
        BigDecimal iva;
        BigDecimal total;
        if (applyIva) {
            rate = requestedRate == null ? DEFAULT_IVA_RATE : requestedRate;
            if (rate.signum() <= 0 || rate.scale() > 6) {
                throw new InvalidWorkOrderRevisionException(
                        "ivaRate must be positive and have at most 6 decimal places");
            }
            rate = rate.setScale(6, RoundingMode.UNNECESSARY);
            iva = normalizedSubtotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            total = normalizedSubtotal.add(iva).setScale(2, RoundingMode.HALF_UP);
        } else {
            if (requestedRate != null && requestedRate.signum() != 0) {
                throw new InvalidWorkOrderRevisionException("ivaRate must be zero when applyIva is false");
            }
            rate = ZERO_RATE;
            iva = ZERO_MONEY;
            total = normalizedSubtotal;
        }
        validateRequested("ivaAmount", requestedIva, iva);
        validateRequested("totalAmount", requestedTotal, total);
        return new QuotationAmounts(normalizedSubtotal, applyIva, rate, iva, total);
    }

    public BigDecimal calculateLineSubtotal(
            BigDecimal quantity, BigDecimal unitPrice, BigDecimal requestedSubtotal) {
        if (quantity == null || quantity.signum() <= 0 || quantity.scale() > 6) {
            throw new InvalidWorkOrderRevisionException(
                    "Line quantity must be positive and have at most 6 decimal places");
        }
        if (unitPrice == null || unitPrice.signum() < 0 || unitPrice.scale() > 4) {
            throw new InvalidWorkOrderRevisionException(
                    "Line unitPrice must be non-negative and have at most 4 decimal places");
        }
        BigDecimal calculated = quantity.multiply(unitPrice).setScale(4, RoundingMode.HALF_UP);
        validateRequested("lineSubtotal", requestedSubtotal, calculated);
        return calculated;
    }

    private void validateRequested(String field, BigDecimal requested, BigDecimal calculated) {
        if (requested != null && requested.compareTo(calculated) != 0) {
            throw new InvalidWorkOrderRevisionException(field + " is inconsistent with server calculation");
        }
    }
}
