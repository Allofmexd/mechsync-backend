package com.mechsync.modules.servicereports.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mechsync.modules.catalogs.infrastructure.persistence.CatalogStatusJpaEntity;
import com.mechsync.modules.catalogs.infrastructure.repository.CatalogStatusJpaRepository;
import com.mechsync.modules.servicereports.application.dto.JobClosure;
import com.mechsync.modules.servicereports.domain.exception.ServiceReportConflictException;
import com.mechsync.modules.servicereports.domain.model.*;
import com.mechsync.modules.servicereports.infrastructure.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ServiceReportPersistenceAdapterTest {
    @Mock ServiceReportJpaRepository reports;
    @Mock CatalogStatusJpaRepository statuses;
    ServiceReportPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ServiceReportPersistenceAdapter(reports, statuses);
    }

    @Test
    void readsCompletedJobClosureWithBigDecimalAmounts() {
        JobClosureView view = mock(JobClosureView.class);
        when(view.getJobId()).thenReturn(1L);
        when(view.getStatusCode()).thenReturn("COMPLETADO");
        when(view.getActualSubtotal()).thenReturn(new BigDecimal("3660.00"));
        when(view.getActualIva()).thenReturn(new BigDecimal("585.60"));
        when(view.getActualTotal()).thenReturn(new BigDecimal("4245.60"));
        when(reports.findJobClosure(1L)).thenReturn(Optional.of(view));

        JobClosure result = adapter.findJobClosure(1L).orElseThrow();

        assertEquals("COMPLETADO", result.status());
        assertEquals(new BigDecimal("4245.60"), result.actualTotal());
    }

    @Test
    void insertsAndMapsServiceReport() {
        CatalogStatusJpaEntity completed = status(21L, "COMPLETADO");
        when(statuses.findAllByContextOrderByIdAsc("SERVICE_REPORTS"))
                .thenReturn(List.of(completed));
        when(reports.saveAndFlush(any())).thenAnswer(invocation -> {
            ServiceReportJpaEntity entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", 9L);
            return entity;
        });

        ServiceReport result = adapter.insert(report(), 21L);

        assertEquals(9L, result.id());
        assertEquals(new BigDecimal("3660.00"), result.finalSubtotal());
        assertEquals(ServiceReportStatus.COMPLETADO, result.status());
    }

    @Test
    void listsAndFindsByIdAndJob() {
        ServiceReportJpaEntity entity = entity();
        ReflectionTestUtils.setField(entity, "id", 9L);
        CatalogStatusJpaEntity completed = status(21L, "COMPLETADO");
        when(statuses.findAllByContextOrderByIdAsc("SERVICE_REPORTS"))
                .thenReturn(List.of(completed));
        when(reports.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(reports.findById(9L)).thenReturn(Optional.of(entity));
        when(reports.findByJobId(1L)).thenReturn(Optional.of(entity));

        assertEquals(1, adapter.findAll(0, 20).content().size());
        assertEquals(1L, adapter.findById(9L).orElseThrow().jobId());
        assertEquals(9L, adapter.findByJobId(1L).orElseThrow().id());
    }

    @Test
    void translatesUniqueConstraintViolationToConflict() {
        when(reports.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("unique"));
        assertThrows(ServiceReportConflictException.class, () -> adapter.insert(report(), 21L));
    }

    @Test
    void assemblesPdfDataFromReadOnlyOperationalProjections() {
        ServiceReportPdfHeaderView header = mock(ServiceReportPdfHeaderView.class);
        ServiceReportPdfServiceLineView service = mock(ServiceReportPdfServiceLineView.class);
        ServiceReportPdfPartLineView part = mock(ServiceReportPdfPartLineView.class);
        LocalDateTime now = LocalDateTime.of(2026, 7, 20, 12, 0);
        when(header.getReportId()).thenReturn(9L);
        when(header.getJobId()).thenReturn(1L);
        when(header.getReportStatus()).thenReturn("COMPLETADO");
        when(header.getReportDate()).thenReturn(now);
        when(header.getFinalDescription()).thenReturn("Trabajo completado");
        when(header.getFinalSubtotal()).thenReturn(new BigDecimal("3660.00"));
        when(header.getFinalIva()).thenReturn(new BigDecimal("585.60"));
        when(header.getFinalTotal()).thenReturn(new BigDecimal("4245.60"));
        when(header.getCustomerConfirmation()).thenReturn(true);
        when(header.getWorkOrderId()).thenReturn(1L);
        when(header.getVehicleIntakeId()).thenReturn(2L);
        when(header.getTechnicianId()).thenReturn(3L);
        when(header.getTechnicianName()).thenReturn("Tecnico QA");
        when(header.getCustomerId()).thenReturn(4L);
        when(header.getCustomerName()).thenReturn("Cliente QA");
        when(header.getVehicleId()).thenReturn(5L);
        when(header.getVehicleBrand()).thenReturn("Nissan");
        when(header.getVehicleModel()).thenReturn("Sentra");
        when(header.getVehicleYear()).thenReturn(2010);
        when(header.getLicensePlate()).thenReturn("QA-001");
        when(header.getVin()).thenReturn("QA-VIN");
        when(header.getMileage()).thenReturn(100000);
        when(service.getName()).thenReturn("Cambio de aceite");
        when(service.getQuantity()).thenReturn(BigDecimal.ONE);
        when(service.getUnitPrice()).thenReturn(new BigDecimal("1200.00"));
        when(service.getSubtotal()).thenReturn(new BigDecimal("1200.00"));
        when(part.getName()).thenReturn("Filtro");
        when(part.getQuantity()).thenReturn(BigDecimal.ONE);
        when(part.getMeasurementUnit()).thenReturn("PIEZA");
        when(part.getUnitPrice()).thenReturn(new BigDecimal("800.00"));
        when(part.getSubtotal()).thenReturn(new BigDecimal("800.00"));
        when(reports.findPdfHeader(9L)).thenReturn(Optional.of(header));
        when(reports.findPdfServices(9L)).thenReturn(List.of(service));
        when(reports.findPdfParts(9L)).thenReturn(List.of(part));

        ServiceReportPdfData result = adapter.findPdfDataByReportId(9L).orElseThrow();

        assertEquals(ServiceReportStatus.COMPLETADO, result.reportStatus());
        assertEquals("Tecnico QA", result.technicianName());
        assertEquals("Cambio de aceite", result.services().get(0).name());
        assertEquals("PIEZA", result.parts().get(0).measurementUnit());
        assertEquals(new BigDecimal("4245.60"), result.finalTotal());
    }

    @Test
    void missingPdfHeaderDoesNotLoadLines() {
        when(reports.findPdfHeader(99L)).thenReturn(Optional.empty());

        assertTrue(adapter.findPdfDataByReportId(99L).isEmpty());
        verify(reports, never()).findPdfServices(anyLong());
        verify(reports, never()).findPdfParts(anyLong());
    }

    private ServiceReport report() {
        LocalDateTime now = LocalDateTime.now();
        return new ServiceReport(null, 1L, ServiceReportStatus.COMPLETADO, now,
                "Trabajo completado", new BigDecimal("3660.00"),
                new BigDecimal("585.60"), new BigDecimal("4245.60"), true,
                null, now, null);
    }

    private ServiceReportJpaEntity entity() {
        LocalDateTime now = LocalDateTime.now();
        return new ServiceReportJpaEntity(1L, now, "Trabajo completado",
                new BigDecimal("3660.00"), new BigDecimal("585.60"),
                new BigDecimal("4245.60"), true, null, 21L, now);
    }

    private CatalogStatusJpaEntity status(Long id, String code) {
        CatalogStatusJpaEntity value = mock(CatalogStatusJpaEntity.class);
        when(value.getId()).thenReturn(id);
        when(value.getCode()).thenReturn(code);
        return value;
    }
}
