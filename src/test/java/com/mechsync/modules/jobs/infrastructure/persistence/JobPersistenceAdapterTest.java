package com.mechsync.modules.jobs.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mechsync.modules.catalogs.infrastructure.persistence.CatalogStatusJpaEntity;
import com.mechsync.modules.catalogs.infrastructure.repository.CatalogStatusJpaRepository;
import com.mechsync.modules.jobs.domain.model.*;
import com.mechsync.modules.jobs.infrastructure.repository.JobJpaRepository;
import com.mechsync.modules.workorders.infrastructure.persistence.*;
import com.mechsync.modules.workorders.infrastructure.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JobPersistenceAdapterTest {
    @Mock JobJpaRepository jobs;
    @Mock WorkOrderJpaRepository workOrders;
    @Mock WorkOrderRevisionJpaRepository revisions;
    @Mock WorkOrderRevisionStatusJpaRepository revisionStatuses;
    @Mock CatalogStatusJpaRepository statuses;
    JobPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JobPersistenceAdapter(jobs, workOrders, revisions, revisionStatuses, statuses);
    }

    @Test
    void insertsJobWithBigDecimalMappingsAndCatalogStatus() {
        CatalogStatusJpaEntity pending = status(11L, "PENDIENTE");
        when(statuses.findAllByContextOrderByIdAsc("JOBS")).thenReturn(List.of(pending));
        when(jobs.saveAndFlush(any())).thenAnswer(invocation -> {
            JobJpaEntity entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", 5L);
            return entity;
        });

        Job saved = adapter.insert(job(null, JobStatus.PENDIENTE), 11L);

        assertEquals(5L, saved.id());
        assertEquals(new BigDecimal("0.00"), saved.realSubtotalAmount());
        assertEquals(JobStatus.PENDIENTE, saved.status());
        verify(jobs).saveAndFlush(any(JobJpaEntity.class));
    }

    @Test
    void listsAndFindsJobsWithoutJpaEntitiesInContract() {
        CatalogStatusJpaEntity pending = status(11L, "PENDIENTE");
        JobJpaEntity entity = entity();
        ReflectionTestUtils.setField(entity, "id", 5L);
        when(statuses.findAllByContextOrderByIdAsc("JOBS")).thenReturn(List.of(pending));
        when(jobs.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(jobs.findById(5L)).thenReturn(Optional.of(entity));

        assertEquals(1, adapter.findAll(0, 20).content().size());
        assertEquals(7L, adapter.findById(5L).orElseThrow().initialApprovedRevisionId());
    }

    @Test
    void resolvesRevisionAuthorizationAndFinalPointer() {
        WorkOrderRevisionJpaEntity revision = new WorkOrderRevisionJpaEntity(1L, 1, 21L, 3L,
                null, null, BigDecimal.ONE, new BigDecimal("100.00"), true,
                new BigDecimal("0.160000"), new BigDecimal("16.00"),
                new BigDecimal("116.00"), "MXN", null, null, null, null, 99L);
        WorkOrderRevisionStatusJpaEntity approved = mock(WorkOrderRevisionStatusJpaEntity.class);
        when(approved.getCode()).thenReturn("APPROVED");
        WorkOrderJpaEntity workOrder = new WorkOrderJpaEntity(1L, 2L, 3L,
                LocalDateTime.now(), null, null, BigDecimal.ONE, new BigDecimal("100.00"),
                new BigDecimal("16.00"), new BigDecimal("116.00"), null, 4L,
                LocalDateTime.now(), null);
        workOrder.setFinalApprovedRevisionId(7L);
        when(revisions.findById(7L)).thenReturn(Optional.of(revision));
        when(revisionStatuses.findById(21L)).thenReturn(Optional.of(approved));
        when(workOrders.findById(1L)).thenReturn(Optional.of(workOrder));

        assertEquals("APPROVED",
                adapter.findRevisionAuthorization(7L).orElseThrow().statusCode());
        assertEquals(7L, adapter.finalApprovedRevisionId(1L).orElseThrow());
    }

    @Test
    void synchronizesActualSubtotalAndTotalWithoutChangingIva() {
        JobJpaEntity entity = entity();
        when(jobs.findByIdForUpdate(5L)).thenReturn(Optional.of(entity));
        when(jobs.saveAndFlush(entity)).thenReturn(entity);

        adapter.updateActualSubtotal(5L, new BigDecimal("200.00"));

        assertEquals(new BigDecimal("200.00"), entity.getActualSubtotal());
        assertEquals(new BigDecimal("200.00"), entity.getActualTotal());
        verify(jobs).saveAndFlush(entity);
    }

    private Job job(Long id, JobStatus status) {
        return new Job(id, 1L, 7L, 3L, status, null, null, null, null, null,
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                "Notes", null, null, null);
    }

    private JobJpaEntity entity() {
        return new JobJpaEntity(1L, 7L, 3L, null, new BigDecimal("0.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), "Notes", 11L);
    }

    private CatalogStatusJpaEntity status(Long id, String code) {
        CatalogStatusJpaEntity value = mock(CatalogStatusJpaEntity.class);
        when(value.getId()).thenReturn(id);
        when(value.getCode()).thenReturn(code);
        return value;
    }
}
