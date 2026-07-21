package com.mechsync.modules.customerportal.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CustomerPortalStatusMapperTest {

    private final CustomerPortalStatusMapper mapper = new CustomerPortalStatusMapper();

    @Test
    void mapsEverySupportedCustomerStatusWithoutExposingCodes() {
        assertEquals("En diagnóstico", mapper.intake("EN_DIAGNOSTICO"));
        assertEquals("En espera de piezas", mapper.intake("EN_ESPERA_PIEZAS"));
        assertEquals("Cotización en preparación", mapper.workOrder("PENDIENTE"));
        assertEquals("Servicio autorizado", mapper.workOrder("APROBADO"));
        assertEquals("Cotización disponible", mapper.quotation("SENT"));
        assertEquals("Cotización autorizada", mapper.quotation("APPROVED"));
        assertEquals("Trabajo programado", mapper.job("PENDIENTE"));
        assertEquals("Trabajo completado", mapper.job("COMPLETADO"));
    }

    @Test
    void unknownStatusUsesSafeText() {
        assertEquals("Estado en actualización", mapper.job("INTERNAL_UNKNOWN"));
        assertEquals("Estado en actualización", mapper.intake(null));
    }

    @Test
    void formatsSpecialtyWithoutExposingCatalogCode() {
        assertEquals("Diagnostico electronico", mapper.specialty("DIAGNOSTICO_ELECTRONICO"));
    }
}
