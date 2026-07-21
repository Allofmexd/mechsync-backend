package com.mechsync.modules.customerportal.application.usecase;

import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomerPortalStatusMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerPortalStatusMapper.class);
    private static final String UNKNOWN = "Estado en actualización";
    private static final Map<String, String> INTAKE_STATUSES = Map.of(
            "EN_DIAGNOSTICO", "En diagnóstico",
            "EN_PROCESO", "En atención",
            "EN_ESPERA_PIEZAS", "En espera de piezas",
            "COMPLETADO", "Ingreso cerrado",
            "CANCELADO", "Cancelado");
    private static final Map<String, String> WORK_ORDER_STATUSES = Map.of(
            "PENDIENTE", "Cotización en preparación",
            "APROBADO", "Servicio autorizado",
            "RECHAZADO", "Servicio no autorizado",
            "EN_PROCESO", "Servicio en proceso",
            "CANCELADO", "Cancelado");
    private static final Map<String, String> REVISION_STATUSES = Map.of(
            "SENT", "Cotización disponible",
            "APPROVED", "Cotización autorizada");
    private static final Map<String, String> JOB_STATUSES = Map.of(
            "PENDIENTE", "Trabajo programado",
            "EN_PROCESO", "Trabajo en proceso",
            "COMPLETADO", "Trabajo completado",
            "CANCELADO", "Cancelado");

    public String intake(String code) {
        return map("VEHICLE_INTAKES", code, INTAKE_STATUSES);
    }

    public String workOrder(String code) {
        return map("WORK_ORDERS", code, WORK_ORDER_STATUSES);
    }

    public String quotation(String code) {
        return map("WORK_ORDER_REVISIONS", code, REVISION_STATUSES);
    }

    public String job(String code) {
        return map("JOBS", code, JOB_STATUSES);
    }

    public String specialty(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String normalized = code.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private String map(String context, String code, Map<String, String> mapping) {
        if (code == null || code.isBlank()) {
            LOGGER.warn("Missing customer portal status for context {}", context);
            return UNKNOWN;
        }
        String visible = mapping.get(code);
        if (visible == null) {
            LOGGER.warn("Unknown customer portal status for context {}", context);
            return UNKNOWN;
        }
        return visible;
    }
}
