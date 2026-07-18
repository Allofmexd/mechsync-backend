package com.mechsync.modules.jobs.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mechsync.modules.jobs.application.dto.*;
import com.mechsync.modules.jobs.application.port.out.*;
import com.mechsync.modules.jobs.domain.exception.*;
import com.mechsync.modules.jobs.domain.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {
    @Mock JobRepositoryPort repository;
    JobService service;

    @BeforeEach
    void setUp() {
        service = new JobService(repository);
    }

    @Test
    void createsJobFromFinalApprovedRevisionWithoutChangingRevision() {
        creationContext("APPROVED", 1L, 7L);
        when(repository.technicianExists(3L)).thenReturn(true);
        when(repository.requireStatusId(JobStatus.PENDIENTE)).thenReturn(11L);
        when(repository.insert(any(), eq(11L))).thenAnswer(invocation -> {
            Job value = invocation.getArgument(0);
            return withId(value, 9L);
        });

        Job result = service.create(command());

        assertEquals(JobStatus.PENDIENTE, result.status());
        assertEquals(new BigDecimal("0.00"), result.realTotalAmount());
        verify(repository, never()).update(any(), anyLong());
    }

    @Test void rejectsMissingWorkOrder() {
        when(repository.workOrderExists(1L)).thenReturn(false);
        assertThrows(JobWorkOrderNotFoundException.class, () -> service.create(command()));
    }

    @Test void rejectsMissingRevision() {
        when(repository.workOrderExists(1L)).thenReturn(true);
        when(repository.findRevisionAuthorization(7L)).thenReturn(Optional.empty());
        assertThrows(JobRevisionNotFoundException.class, () -> service.create(command()));
    }

    @Test void rejectsRevisionFromAnotherWorkOrder() {
        when(repository.workOrderExists(1L)).thenReturn(true);
        when(repository.findRevisionAuthorization(7L))
                .thenReturn(Optional.of(new JobRevisionAuthorization(2L, "APPROVED")));
        assertThrows(InvalidJobException.class, () -> service.create(command()));
    }

    @Test void rejectsRevisionThatIsNotApproved() {
        creationContext("SENT", 1L, 7L);
        assertThrows(InvalidJobException.class, () -> service.create(command()));
    }

    @Test void rejectsRevisionThatIsNotFinalApproved() {
        creationContext("APPROVED", 1L, 8L);
        assertThrows(InvalidJobException.class, () -> service.create(command()));
    }

    @Test void rejectsUnknownTechnician() {
        creationContext("APPROVED", 1L, 7L);
        when(repository.technicianExists(3L)).thenReturn(false);
        assertThrows(JobTechnicianNotFoundException.class, () -> service.create(command()));
    }

    @Test void rejectsDuplicateAuthorizedRevision() {
        creationContext("APPROVED", 1L, 7L);
        when(repository.technicianExists(3L)).thenReturn(true);
        when(repository.existsByWorkOrderId(1L)).thenReturn(true);
        assertThrows(JobConflictException.class, () -> service.create(command()));
    }

    @Test void startsPendingJob() {
        Job pending = job(JobStatus.PENDIENTE, null);
        when(repository.findByIdForUpdate(9L)).thenReturn(Optional.of(pending));
        when(repository.requireStatusId(JobStatus.EN_PROCESO)).thenReturn(12L);
        when(repository.update(any(), eq(12L))).thenAnswer(invocation -> invocation.getArgument(0));
        Job result = service.start(9L);
        assertEquals(JobStatus.EN_PROCESO, result.status());
        assertNotNull(result.startDate());
    }

    @Test void cannotStartTerminalJob() {
        when(repository.findByIdForUpdate(9L))
                .thenReturn(Optional.of(job(JobStatus.COMPLETADO, LocalDateTime.now())));
        assertThrows(JobConflictException.class, () -> service.start(9L));
    }

    @Test void completesStartedJobWithValidatedBigDecimalAmounts() {
        Job active = job(JobStatus.EN_PROCESO, LocalDateTime.now().minusHours(1));
        when(repository.findByIdForUpdate(9L)).thenReturn(Optional.of(active));
        when(repository.requireStatusId(JobStatus.COMPLETADO)).thenReturn(13L);
        when(repository.update(any(), eq(13L))).thenAnswer(invocation -> invocation.getArgument(0));
        Job result = service.complete(new CompleteJobCommand(9L, new BigDecimal("100.00"),
                new BigDecimal("16.00"), new BigDecimal("116.00"), "Done"));
        assertEquals(JobStatus.COMPLETADO, result.status());
        assertEquals(new BigDecimal("116.00"), result.realTotalAmount());
        assertNotNull(result.completionDate());
    }

    @Test void rejectsCompletionWithoutStart() {
        when(repository.findByIdForUpdate(9L))
                .thenReturn(Optional.of(job(JobStatus.EN_PROCESO, null)));
        assertThrows(JobConflictException.class, () -> service.complete(new CompleteJobCommand(
                9L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null)));
    }

    @Test void rejectsNegativeOrInconsistentAmounts() {
        Job active = job(JobStatus.EN_PROCESO, LocalDateTime.now().minusHours(1));
        when(repository.findByIdForUpdate(9L)).thenReturn(Optional.of(active));
        assertThrows(InvalidJobException.class, () -> service.complete(new CompleteJobCommand(
                9L, new BigDecimal("-1.00"), BigDecimal.ZERO, BigDecimal.ZERO, null)));
        assertThrows(InvalidJobException.class, () -> service.complete(new CompleteJobCommand(
                9L, new BigDecimal("10.00"), new BigDecimal("1.60"), new BigDecimal("99.00"), null)));
    }

    @Test void cancelsPendingAndInProgressJobs() {
        for (JobStatus status : new JobStatus[] {JobStatus.PENDIENTE, JobStatus.EN_PROCESO}) {
            Job source = job(status, status == JobStatus.EN_PROCESO ? LocalDateTime.now() : null);
            when(repository.findByIdForUpdate(9L)).thenReturn(Optional.of(source));
            when(repository.requireStatusId(JobStatus.CANCELADO)).thenReturn(14L);
            when(repository.update(any(), eq(14L))).thenAnswer(invocation -> invocation.getArgument(0));
            Job result = service.cancel(new CancelJobCommand(9L, "Customer decision"));
            assertEquals(JobStatus.CANCELADO, result.status());
            assertNotNull(result.cancelledAt());
            assertNull(result.completionDate());
        }
    }

    @Test void cannotCancelCompletedJob() {
        when(repository.findByIdForUpdate(9L))
                .thenReturn(Optional.of(job(JobStatus.COMPLETADO, LocalDateTime.now())));
        assertThrows(JobConflictException.class,
                () -> service.cancel(new CancelJobCommand(9L, null)));
    }

    private void creationContext(String revisionStatus, Long revisionWorkOrder, Long finalRevision) {
        when(repository.workOrderExists(1L)).thenReturn(true);
        when(repository.findRevisionAuthorization(7L))
                .thenReturn(Optional.of(new JobRevisionAuthorization(revisionWorkOrder, revisionStatus)));
        if (Long.valueOf(1L).equals(revisionWorkOrder) && "APPROVED".equals(revisionStatus)) {
            when(repository.finalApprovedRevisionId(1L)).thenReturn(Optional.of(finalRevision));
        }
    }

    private CreateJobCommand command() {
        return new CreateJobCommand(1L, 7L, 3L, LocalDateTime.now().plusDays(1), "Authorized");
    }

    private Job job(JobStatus status, LocalDateTime start) {
        return new Job(9L, 1L, 7L, 3L, status, null, start, null, null, null,
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                "Notes", null, LocalDateTime.now().minusDays(1), null);
    }

    private Job withId(Job value, Long id) {
        return new Job(id, value.workOrderId(), value.initialApprovedRevisionId(),
                value.technicianId(), value.status(), value.scheduledStartDate(), value.startDate(),
                value.completionDate(), value.cancelledAt(), value.actualHours(),
                value.realSubtotalAmount(), value.realIvaAmount(), value.realTotalAmount(),
                value.notes(), value.cancellationNotes(), value.createdAt(), value.updatedAt());
    }
}
