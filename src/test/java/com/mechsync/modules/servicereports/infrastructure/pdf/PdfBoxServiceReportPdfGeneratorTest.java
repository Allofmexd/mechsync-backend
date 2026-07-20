package com.mechsync.modules.servicereports.infrastructure.pdf;

import static org.junit.jupiter.api.Assertions.*;

import com.mechsync.modules.servicereports.domain.model.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

class PdfBoxServiceReportPdfGeneratorTest {
    private final PdfBoxServiceReportPdfGenerator generator =
            new PdfBoxServiceReportPdfGenerator();

    @Test
    void generatesValidPdfWithOperationalLinesAndMoney() throws Exception {
        ServiceReportPdfData.ServiceLine service = new ServiceReportPdfData.ServiceLine(
                "Cambio de aceite", new BigDecimal("1.00"), new BigDecimal("1200.00"),
                new BigDecimal("1200.00"));
        ServiceReportPdfData.PartLine part = new ServiceReportPdfData.PartLine(
                "Filtro de transmision", new BigDecimal("1.00"), "PIEZA",
                new BigDecimal("800.00"), new BigDecimal("800.00"));

        byte[] bytes = generator.generate(data(List.of(service), List.of(part)));

        assertTrue(bytes.length > 500);
        assertEquals("%PDF", new String(bytes, 0, 4, StandardCharsets.US_ASCII));
        try (PDDocument document = Loader.loadPDF(bytes)) {
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("Reporte de servicio"));
            assertTrue(text.contains("REP-9"));
            assertTrue(text.contains("Cambio de aceite"));
            assertTrue(text.contains("Filtro de transmision"));
            assertTrue(text.contains("4,245.60"));
            assertTrue(text.contains("Página 1 de"));
        }
    }

    @Test
    void rendersExplicitMessagesForEmptyLineLists() throws Exception {
        byte[] bytes = generator.generate(data(List.of(), List.of()));

        try (PDDocument document = Loader.loadPDF(bytes)) {
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("Sin servicios reales registrados."));
            assertTrue(text.contains("Sin piezas reales registradas."));
        }
    }

    private ServiceReportPdfData data(List<ServiceReportPdfData.ServiceLine> services,
            List<ServiceReportPdfData.PartLine> parts) {
        LocalDateTime now = LocalDateTime.of(2026, 7, 20, 12, 0);
        return new ServiceReportPdfData(9L, 1L, ServiceReportStatus.COMPLETADO, now,
                "Trabajo completado correctamente.", new BigDecimal("3660.00"),
                new BigDecimal("585.60"), new BigDecimal("4245.60"), true, null,
                1L, 1L, 1L, "Tecnico QA", 6L, "Cliente QA", 1L, "Nissan",
                "Sentra", 2010, "QA-001", "QA-VIN", 100000, services, parts);
    }
}
