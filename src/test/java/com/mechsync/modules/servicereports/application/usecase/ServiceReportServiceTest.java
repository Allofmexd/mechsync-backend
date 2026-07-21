package com.mechsync.modules.servicereports.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mechsync.modules.servicereports.application.dto.*;
import com.mechsync.modules.servicereports.application.port.out.ServiceReportRepositoryPort;
import com.mechsync.modules.servicereports.domain.exception.*;
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
class ServiceReportServiceTest {
    @Mock ServiceReportRepositoryPort repository;
    ServiceReportService service;

    @BeforeEach
    void setUp() {
        service = new ServiceReportService(repository);
    }

    @Test
    void createsImmutableReportFromCompletedJobUsingJobMoney() {
        when(repository.findJobClosure(1L)).thenReturn(Optional.of(completedJob()));
        when(repository.existsByJobId(1L)).thenReturn(false);
        when(repository.requireStatusId(ServiceReportStatus.COMPLETADO)).thenReturn(21L);
        when(repository.insert(any(), eq(21L))).thenAnswer(invocation ->
                withId(invocation.getArgument(0), 9L));

        ServiceReport result = service.create(command(null));

        assertEquals(9L, result.id());
        assertEquals(ServiceReportStatus.COMPLETADO, result.status());
        assertEquals(new BigDecimal("3660.00"), result.finalSubtotal());
        assertEquals(new BigDecimal("585.60"), result.finalIva());
        assertEquals(new BigDecimal("4245.60"), result.finalTotal());
        verify(repository).insert(any(ServiceReport.class), eq(21L));
    }

    @Test
    void deliveredAtCreatesDeliveredReport() {
        when(repository.findJobClosure(1L)).thenReturn(Optional.of(completedJob()));
        when(repository.existsByJobId(1L)).thenReturn(false);
        when(repository.requireStatusId(ServiceReportStatus.ENTREGADO)).thenReturn(22L);
        when(repository.insert(any(), eq(22L))).thenAnswer(invocation -> invocation.getArgument(0));
        LocalDateTime deliveredAt = LocalDateTime.now();

        ServiceReport result = service.create(command(deliveredAt));

        assertEquals(ServiceReportStatus.ENTREGADO, result.status());
        assertEquals(deliveredAt, result.deliveredAt());
    }

    @Test
    void rejectsMissingJob() {
        when(repository.findJobClosure(1L)).thenReturn(Optional.empty());
        assertThrows(ServiceReportJobNotFoundException.class,
                () -> service.create(command(null)));
    }

    @Test
    void rejectsEveryNonCompletedJobStatus() {
        for (String status : List.of("PENDIENTE", "EN_PROCESO", "CANCELADO")) {
            reset(repository);
            when(repository.findJobClosure(1L)).thenReturn(Optional.of(new JobClosure(1L, status,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)));
            assertThrows(ServiceReportConflictException.class,
                    () -> service.create(command(null)), status);
            verify(repository, never()).insert(any(), anyLong());
        }
    }

    @Test
    void rejectsDuplicateReportForJob() {
        when(repository.findJobClosure(1L)).thenReturn(Optional.of(completedJob()));
        when(repository.existsByJobId(1L)).thenReturn(true);
        assertThrows(ServiceReportConflictException.class,
                () -> service.create(command(null)));
    }

    @Test
    void queriesByIdAndJobAndReportsNotFound() {
        ServiceReport report = report();
        when(repository.findById(9L)).thenReturn(Optional.of(report));
        when(repository.findByJobId(1L)).thenReturn(Optional.of(report));
        assertEquals(report, service.get(9L));
        assertEquals(report, service.getByJobId(1L));

        when(repository.findById(99L)).thenReturn(Optional.empty());
        when(repository.findByJobId(99L)).thenReturn(Optional.empty());
        assertThrows(ServiceReportNotFoundException.class, () -> service.get(99L));
        assertThrows(ServiceReportNotFoundException.class, () -> service.getByJobId(99L));
    }

    @Test
    void listsReportsWithPagination() {
        ServiceReportPage page = new ServiceReportPage(List.of(report()), 0, 20, 1, 1);
        when(repository.findAll(0, 20)).thenReturn(page);
        assertEquals(page, service.list(0, 20));
    }

    @Test
    void listsAndGetsReportsAssignedToTechnician() {
        ServiceReport report = report();
        ServiceReportPage page = new ServiceReportPage(List.of(report), 0, 20, 1, 1);
        when(repository.findAllByTechnicianId(3L, 0, 20)).thenReturn(page);
        when(repository.findByIdAndTechnicianId(9L, 3L)).thenReturn(Optional.of(report));
        when(repository.findByJobIdAndTechnicianId(1L, 3L)).thenReturn(Optional.of(report));

        assertEquals(page, service.listAssignedTo(3L, 0, 20));
        assertEquals(report, service.getAssignedTo(9L, 3L));
        assertEquals(report, service.getByJobIdAssignedTo(1L, 3L));
    }

    @Test
    void hidesForeignReportsAndJobsAsNotFound() {
        when(repository.findByIdAndTechnicianId(9L, 4L)).thenReturn(Optional.empty());
        when(repository.findByJobIdAndTechnicianId(1L, 4L)).thenReturn(Optional.empty());

        assertThrows(ServiceReportNotFoundException.class,
                () -> service.getAssignedTo(9L, 4L));
        assertThrows(ServiceReportNotFoundException.class,
                () -> service.getByJobIdAssignedTo(1L, 4L));
    }

    private CreateServiceReportCommand command(LocalDateTime deliveredAt) {
        return new CreateServiceReportCommand(1L, "  Trabajo completado correctamente.  ",
                true, deliveredAt);
    }

    private JobClosure completedJob() {
        return new JobClosure(1L, "COMPLETADO", new BigDecimal("3660.00"),
                new BigDecimal("585.60"), new BigDecimal("4245.60"));
    }

    private ServiceReport report() {
        LocalDateTime now = LocalDateTime.now();
        return new ServiceReport(9L, 1L, ServiceReportStatus.COMPLETADO, now,
                "Trabajo completado correctamente.", new BigDecimal("3660.00"),
                new BigDecimal("585.60"), new BigDecimal("4245.60"), true,
                null, now, null);
    }

    private ServiceReport withId(ServiceReport value, Long id) {
        return new ServiceReport(id, value.jobId(), value.status(), value.reportDate(),
                value.finalDescription(), value.finalSubtotal(), value.finalIva(),
                value.finalTotal(), value.customerConfirmation(), value.deliveredAt(),
                value.createdAt(), value.updatedAt());
    }
}
