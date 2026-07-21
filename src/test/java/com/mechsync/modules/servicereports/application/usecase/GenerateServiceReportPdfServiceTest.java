package com.mechsync.modules.servicereports.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mechsync.modules.servicereports.application.port.out.ServiceReportPdfDataPort;
import com.mechsync.modules.servicereports.application.port.out.ServiceReportPdfGeneratorPort;
import com.mechsync.modules.servicereports.domain.exception.ServiceReportNotFoundException;
import com.mechsync.modules.servicereports.domain.exception.ServiceReportPdfGenerationException;
import com.mechsync.modules.servicereports.domain.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateServiceReportPdfServiceTest {
    @Mock ServiceReportPdfDataPort dataPort;
    @Mock ServiceReportPdfGeneratorPort generator;
    GenerateServiceReportPdfService service;

    @BeforeEach
    void setUp() {
        service = new GenerateServiceReportPdfService(dataPort, generator);
    }

    @Test
    void generatesInMemoryPdfForExistingReportWithoutChangingSourceData() {
        ServiceReportPdfData data = data(List.of(), List.of());
        byte[] bytes = "%PDF-1.7".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        when(dataPort.findPdfDataByReportId(9L)).thenReturn(Optional.of(data));
        when(generator.generate(data)).thenReturn(bytes);

        var result = service.generate(9L);

        assertEquals("service-report-9.pdf", result.filename());
        assertArrayEquals(bytes, result.content());
        assertEquals(new BigDecimal("4245.60"), data.finalTotal());
        verify(dataPort).findPdfDataByReportId(9L);
        verify(generator).generate(data);
        verifyNoMoreInteractions(dataPort, generator);
    }

    @Test
    void missingReportIs404AndGeneratorIsNotCalled() {
        when(dataPort.findPdfDataByReportId(99L)).thenReturn(Optional.empty());

        assertThrows(ServiceReportNotFoundException.class, () -> service.generate(99L));
        verifyNoInteractions(generator);
    }

    @Test
    void generatesPdfOnlyWhenReportIsAssignedToTechnician() {
        ServiceReportPdfData data = data(List.of(), List.of());
        byte[] bytes = "%PDF-1.7".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        when(dataPort.findPdfDataByReportIdAndTechnicianId(9L, 3L))
                .thenReturn(Optional.of(data));
        when(generator.generate(data)).thenReturn(bytes);

        var result = service.generateAssignedTo(9L, 3L);

        assertArrayEquals(bytes, result.content());
        verify(dataPort).findPdfDataByReportIdAndTechnicianId(9L, 3L);
    }

    @Test
    void foreignReportPdfIsHiddenAsNotFound() {
        when(dataPort.findPdfDataByReportIdAndTechnicianId(9L, 4L))
                .thenReturn(Optional.empty());

        assertThrows(ServiceReportNotFoundException.class,
                () -> service.generateAssignedTo(9L, 4L));
        verifyNoInteractions(generator);
    }

    @Test
    void rejectsEmptyGeneratorResult() {
        ServiceReportPdfData data = data(List.of(), List.of());
        when(dataPort.findPdfDataByReportId(9L)).thenReturn(Optional.of(data));
        when(generator.generate(data)).thenReturn(new byte[0]);

        assertThrows(ServiceReportPdfGenerationException.class, () -> service.generate(9L));
    }

    private ServiceReportPdfData data(List<ServiceReportPdfData.ServiceLine> services,
            List<ServiceReportPdfData.PartLine> parts) {
        LocalDateTime now = LocalDateTime.of(2026, 7, 20, 12, 0);
        return new ServiceReportPdfData(9L, 1L, ServiceReportStatus.COMPLETADO, now,
                "Trabajo completado", new BigDecimal("3660.00"),
                new BigDecimal("585.60"), new BigDecimal("4245.60"), true, null,
                1L, 1L, 1L, "Tecnico QA", 6L, "Cliente QA", 1L, "Nissan",
                "Sentra", 2010, "QA-001", "QA-VIN", 100000, services, parts);
    }
}
