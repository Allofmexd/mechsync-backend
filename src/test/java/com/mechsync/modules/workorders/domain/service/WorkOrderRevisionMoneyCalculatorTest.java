package com.mechsync.modules.workorders.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import com.mechsync.modules.workorders.domain.exception.InvalidWorkOrderRevisionException;
import com.mechsync.modules.workorders.domain.model.QuotationAmounts;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class WorkOrderRevisionMoneyCalculatorTest {
    private final WorkOrderRevisionMoneyCalculator calculator = new WorkOrderRevisionMoneyCalculator();

    @Test
    void calculatesDefaultIvaAndRoundsContractualAmounts() {
        QuotationAmounts result = calculator.calculate(
                new BigDecimal("100.01"), true, null, new BigDecimal("16.00"),
                new BigDecimal("116.01"));
        assertEquals(new BigDecimal("0.160000"), result.ivaRate());
        assertEquals(new BigDecimal("16.00"), result.ivaAmount());
        assertEquals(new BigDecimal("116.01"), result.totalAmount());
    }

    @Test
    void calculatesWithoutIva() {
        QuotationAmounts result = calculator.calculate(
                new BigDecimal("2500.00"), false, null, BigDecimal.ZERO,
                new BigDecimal("2500.00"));
        assertEquals(0, result.ivaAmount().compareTo(BigDecimal.ZERO));
        assertEquals(new BigDecimal("2500.00"), result.totalAmount());
    }

    @Test
    void rejectsClientTotalThatDoesNotMatchServerCalculation() {
        assertThrows(InvalidWorkOrderRevisionException.class, () -> calculator.calculate(
                new BigDecimal("100.00"), true, null, new BigDecimal("16.00"),
                new BigDecimal("115.00")));
    }

    @Test
    void calculatesLineAtFourDecimals() {
        assertEquals(new BigDecimal("0.9999"), calculator.calculateLineSubtotal(
                new BigDecimal("3.000000"), new BigDecimal("0.3333"), null));
    }

    @Test
    void derivesHeaderSubtotalFromLinesAndValidatesClientValue() {
        QuotationAmounts result = calculator.calculateFromLines(
                List.of(new BigDecimal("1200.0000"), new BigDecimal("800.0000")),
                new BigDecimal("2000.00"), true, null,
                new BigDecimal("320.00"), new BigDecimal("2320.00"));
        assertEquals(new BigDecimal("2000.00"), result.subtotalAmount());
        assertThrows(InvalidWorkOrderRevisionException.class, () -> calculator.calculateFromLines(
                List.of(new BigDecimal("1200.0000"), new BigDecimal("800.0000")),
                new BigDecimal("2500.00"), true, null, null, null));
    }

    @Test
    void rejectsFloatLikeExcessScaleAtContractBoundary() {
        assertThrows(InvalidWorkOrderRevisionException.class, () -> calculator.calculate(
                new BigDecimal("1.001"), false, null, null, null));
    }
}
