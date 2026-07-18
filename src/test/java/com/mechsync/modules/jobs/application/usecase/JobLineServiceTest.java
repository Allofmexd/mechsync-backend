package com.mechsync.modules.jobs.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mechsync.modules.jobs.application.dto.UpsertJobPartLineCommand;
import com.mechsync.modules.jobs.application.dto.UpsertJobServiceLineCommand;
import com.mechsync.modules.jobs.application.port.out.JobLineRepositoryPort;
import com.mechsync.modules.jobs.application.port.out.JobRepositoryPort;
import com.mechsync.modules.jobs.domain.exception.InvalidJobException;
import com.mechsync.modules.jobs.domain.exception.JobConflictException;
import com.mechsync.modules.jobs.domain.exception.JobLineCatalogNotFoundException;
import com.mechsync.modules.jobs.domain.exception.JobLineNotFoundException;
import com.mechsync.modules.jobs.domain.model.Job;
import com.mechsync.modules.jobs.domain.model.JobPartLine;
import com.mechsync.modules.jobs.domain.model.JobServiceLine;
import com.mechsync.modules.jobs.domain.model.JobStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobLineServiceTest {
    @Mock JobRepositoryPort jobs;
    @Mock JobLineRepositoryPort lines;
    JobLineService service;

    @BeforeEach
    void setUp() {
        service = new JobLineService(jobs, lines);
    }

    @Test
    void addsServiceToPendingJobAndCalculatesSubtotalInBackend() {
        when(jobs.findByIdForUpdate(1L)).thenReturn(Optional.of(job(JobStatus.PENDIENTE)));
        when(lines.findServiceName(2L)).thenReturn(Optional.of("Alignment"));
        when(lines.saveService(any())).thenAnswer(invocation -> {
            JobServiceLine value = invocation.getArgument(0);
            return new JobServiceLine(10L, value.jobId(), value.serviceId(), value.serviceName(),
                    value.quantity(), value.unitPrice(), value.lineSubtotal(), LocalDateTime.now(), null);
        });
        when(lines.calculateActualSubtotal(1L)).thenReturn(new BigDecimal("300.00"));

        JobServiceLine result = service.addService(serviceCommand(null, "2.00", "150.00"));

        assertEquals(new BigDecimal("300.00"), result.lineSubtotal());
        verify(jobs).updateActualSubtotal(1L, new BigDecimal("300.00"));
    }

    @Test
    void addsPartToInProgressJob() {
        when(jobs.findByIdForUpdate(1L)).thenReturn(Optional.of(job(JobStatus.EN_PROCESO)));
        when(lines.findPartName(3L)).thenReturn(Optional.of("Filter"));
        when(lines.savePart(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(lines.calculateActualSubtotal(1L)).thenReturn(new BigDecimal("80.00"));

        JobPartLine result = service.addPart(partCommand(null, "1.00", "80.00"));

        assertEquals(new BigDecimal("80.00"), result.lineSubtotal());
        verify(jobs).updateActualSubtotal(1L, new BigDecimal("80.00"));
    }

    @Test
    void rejectsLineChangesForCompletedAndCancelledJobs() {
        for (JobStatus status : new JobStatus[] {JobStatus.COMPLETADO, JobStatus.CANCELADO}) {
            when(jobs.findByIdForUpdate(1L)).thenReturn(Optional.of(job(status)));
            assertThrows(JobConflictException.class,
                    () -> service.addService(serviceCommand(null, "1.00", "10.00")));
            assertThrows(JobConflictException.class,
                    () -> service.addPart(partCommand(null, "1.00", "10.00")));
        }
        verify(lines, never()).saveService(any());
        verify(lines, never()).savePart(any());
    }

    @Test
    void rejectsInvalidQuantityPriceAndScale() {
        when(jobs.findByIdForUpdate(1L)).thenReturn(Optional.of(job(JobStatus.PENDIENTE)));
        when(lines.findServiceName(2L)).thenReturn(Optional.of("Alignment"));

        assertThrows(InvalidJobException.class,
                () -> service.addService(serviceCommand(null, "0.00", "10.00")));
        assertThrows(InvalidJobException.class,
                () -> service.addService(serviceCommand(null, "1.00", "-0.01")));
        assertThrows(InvalidJobException.class,
                () -> service.addService(serviceCommand(null, "1.001", "10.00")));
    }

    @Test
    void rejectsMissingCatalogItems() {
        when(jobs.findByIdForUpdate(1L)).thenReturn(Optional.of(job(JobStatus.PENDIENTE)));
        when(lines.findServiceName(2L)).thenReturn(Optional.empty());
        when(lines.findPartName(3L)).thenReturn(Optional.empty());

        assertThrows(JobLineCatalogNotFoundException.class,
                () -> service.addService(serviceCommand(null, "1.00", "10.00")));
        assertThrows(JobLineCatalogNotFoundException.class,
                () -> service.addPart(partCommand(null, "1.00", "10.00")));
    }

    @Test
    void rejectsInvalidPartValuesAndDuplicateCatalogLines() {
        when(jobs.findByIdForUpdate(1L)).thenReturn(Optional.of(job(JobStatus.PENDIENTE)));
        when(lines.findPartName(3L)).thenReturn(Optional.of("Filter"));

        assertThrows(InvalidJobException.class,
                () -> service.addPart(partCommand(null, "0.00", "10.00")));
        assertThrows(InvalidJobException.class,
                () -> service.addPart(partCommand(null, "1.00", "-0.01")));

        when(lines.partAlreadyUsed(1L, 3L, null)).thenReturn(true);
        assertThrows(JobConflictException.class,
                () -> service.addPart(partCommand(null, "1.00", "10.00")));

        when(lines.findServiceName(2L)).thenReturn(Optional.of("Alignment"));
        when(lines.serviceAlreadyUsed(1L, 2L, null)).thenReturn(true);
        assertThrows(JobConflictException.class,
                () -> service.addService(serviceCommand(null, "1.00", "10.00")));
    }

    @Test
    void updatesAndDeletesServiceOnlyWhenLineBelongsToJob() {
        JobServiceLine current = serviceLine(10L);
        when(jobs.findByIdForUpdate(1L)).thenReturn(Optional.of(job(JobStatus.EN_PROCESO)));
        when(lines.findServiceByIdAndJobIdForUpdate(10L, 1L))
                .thenReturn(Optional.of(current));
        when(lines.findServiceName(2L)).thenReturn(Optional.of("Alignment"));
        when(lines.saveService(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(lines.calculateActualSubtotal(1L)).thenReturn(new BigDecimal("125.00"));

        JobServiceLine updated = service.updateService(serviceCommand(10L, "1.00", "125.00"));
        service.deleteService(1L, 10L);

        assertEquals(new BigDecimal("125.00"), updated.lineSubtotal());
        verify(lines).deleteService(10L, 1L);
        verify(jobs, times(2)).updateActualSubtotal(1L, new BigDecimal("125.00"));
    }

    @Test
    void rejectsServiceLineFromAnotherJobAsNotFound() {
        when(jobs.findByIdForUpdate(1L)).thenReturn(Optional.of(job(JobStatus.PENDIENTE)));
        when(lines.findServiceByIdAndJobIdForUpdate(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(JobLineNotFoundException.class,
                () -> service.updateService(serviceCommand(99L, "1.00", "10.00")));
        assertThrows(JobLineNotFoundException.class, () -> service.deleteService(1L, 99L));
    }

    @Test
    void updatesAndDeletesPartOnlyWhenLineBelongsToJob() {
        JobPartLine current = partLine(20L);
        when(jobs.findByIdForUpdate(1L)).thenReturn(Optional.of(job(JobStatus.PENDIENTE)));
        when(lines.findPartByIdAndJobIdForUpdate(20L, 1L)).thenReturn(Optional.of(current));
        when(lines.findPartName(3L)).thenReturn(Optional.of("Filter"));
        when(lines.savePart(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(lines.calculateActualSubtotal(1L)).thenReturn(new BigDecimal("90.00"));

        JobPartLine updated = service.updatePart(partCommand(20L, "2.00", "45.00"));
        service.deletePart(1L, 20L);

        assertEquals(new BigDecimal("90.00"), updated.lineSubtotal());
        verify(lines).deletePart(20L, 1L);
        verify(jobs, times(2)).updateActualSubtotal(1L, new BigDecimal("90.00"));
    }

    @Test
    void rejectsPartLineFromAnotherJobAsNotFound() {
        when(jobs.findByIdForUpdate(1L)).thenReturn(Optional.of(job(JobStatus.PENDIENTE)));
        when(lines.findPartByIdAndJobIdForUpdate(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(JobLineNotFoundException.class,
                () -> service.updatePart(partCommand(99L, "1.00", "10.00")));
        assertThrows(JobLineNotFoundException.class, () -> service.deletePart(1L, 99L));
    }

    @Test
    void listingLinesDoesNotRequireMutableJobOrTouchQuotation() {
        when(jobs.findById(1L)).thenReturn(Optional.of(job(JobStatus.COMPLETADO)));
        when(lines.findServicesByJobId(1L)).thenReturn(java.util.List.of(serviceLine(10L)));
        when(lines.findPartsByJobId(1L)).thenReturn(java.util.List.of(partLine(20L)));

        assertEquals(1, service.listServices(1L).size());
        assertEquals(1, service.listParts(1L).size());
        verify(jobs, never()).update(any(), any());
    }

    private UpsertJobServiceLineCommand serviceCommand(Long lineId, String quantity, String price) {
        return new UpsertJobServiceLineCommand(1L, lineId, 2L,
                new BigDecimal(quantity), new BigDecimal(price));
    }

    private UpsertJobPartLineCommand partCommand(Long lineId, String quantity, String price) {
        return new UpsertJobPartLineCommand(1L, lineId, 3L,
                new BigDecimal(quantity), new BigDecimal(price));
    }

    private Job job(JobStatus status) {
        return new Job(1L, 5L, 7L, 3L, status, null, null, null, null, null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, null,
                LocalDateTime.now(), null);
    }

    private JobServiceLine serviceLine(Long id) {
        return new JobServiceLine(id, 1L, 2L, "Alignment", BigDecimal.ONE,
                new BigDecimal("100.00"), new BigDecimal("100.00"), LocalDateTime.now(), null);
    }

    private JobPartLine partLine(Long id) {
        return new JobPartLine(id, 1L, 3L, "Filter", BigDecimal.ONE,
                new BigDecimal("80.00"), new BigDecimal("80.00"), LocalDateTime.now(), null);
    }
}
