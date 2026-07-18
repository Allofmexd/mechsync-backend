package com.mechsync.modules.jobs.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mechsync.modules.jobs.domain.model.JobPartLine;
import com.mechsync.modules.jobs.domain.model.JobServiceLine;
import com.mechsync.modules.jobs.infrastructure.repository.JobPartLineJpaRepository;
import com.mechsync.modules.jobs.infrastructure.repository.JobPartLineView;
import com.mechsync.modules.jobs.infrastructure.repository.JobServiceLineJpaRepository;
import com.mechsync.modules.jobs.infrastructure.repository.JobServiceLineView;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JobLinePersistenceAdapterTest {
    @Mock JobServiceLineJpaRepository services;
    @Mock JobPartLineJpaRepository parts;
    @Mock JobServiceLineView serviceView;
    @Mock JobPartLineView partView;
    JobLinePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JobLinePersistenceAdapter(services, parts);
    }

    @Test
    void listsServiceAndPartLinesUsingJoinedViews() {
        stubServiceView();
        stubPartView();
        when(services.findViewsByJobId(1L)).thenReturn(List.of(serviceView));
        when(parts.findViewsByJobId(1L)).thenReturn(List.of(partView));

        JobServiceLine service = adapter.findServicesByJobId(1L).get(0);
        JobPartLine part = adapter.findPartsByJobId(1L).get(0);

        assertEquals("Alignment", service.serviceName());
        assertEquals("Filter", part.partName());
    }

    @Test
    void savesAndUpdatesServiceLineWithBigDecimalMappings() {
        when(services.saveAndFlush(any())).thenAnswer(invocation -> {
            JobServiceLineJpaEntity entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", 10L);
            return entity;
        });
        JobServiceLine inserted = adapter.saveService(serviceLine(null, "120.00"));

        JobServiceLineJpaEntity existing = new JobServiceLineJpaEntity(1L, 2L,
                BigDecimal.ONE, new BigDecimal("120.00"), new BigDecimal("120.00"));
        ReflectionTestUtils.setField(existing, "id", 10L);
        when(services.findByIdAndJobIdForUpdate(10L, 1L)).thenReturn(Optional.of(existing));
        JobServiceLine updated = adapter.saveService(serviceLine(10L, "130.00"));

        assertEquals(10L, inserted.id());
        assertEquals(new BigDecimal("130.00"), updated.lineSubtotal());
        assertEquals(new BigDecimal("130.00"), existing.getActualSubtotal());
    }

    @Test
    void savesAndUpdatesPartLineWithBigDecimalMappings() {
        when(parts.saveAndFlush(any())).thenAnswer(invocation -> {
            JobPartLineJpaEntity entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", 20L);
            return entity;
        });
        JobPartLine inserted = adapter.savePart(partLine(null, "80.00"));

        JobPartLineJpaEntity existing = new JobPartLineJpaEntity(1L, 3L,
                BigDecimal.ONE, new BigDecimal("80.00"), new BigDecimal("80.00"));
        ReflectionTestUtils.setField(existing, "id", 20L);
        when(parts.findByIdAndJobIdForUpdate(20L, 1L)).thenReturn(Optional.of(existing));
        JobPartLine updated = adapter.savePart(partLine(20L, "90.00"));

        assertEquals(20L, inserted.id());
        assertEquals(new BigDecimal("90.00"), updated.lineSubtotal());
        assertEquals(new BigDecimal("90.00"), existing.getActualSubtotal());
    }

    @Test
    void deletesOnlyLinesResolvedByJobAndCalculatesCombinedSubtotal() {
        JobServiceLineJpaEntity service = new JobServiceLineJpaEntity(1L, 2L, BigDecimal.ONE,
                BigDecimal.TEN, BigDecimal.TEN);
        JobPartLineJpaEntity part = new JobPartLineJpaEntity(1L, 3L, BigDecimal.ONE,
                BigDecimal.TEN, BigDecimal.TEN);
        when(services.findByIdAndJobIdForUpdate(10L, 1L)).thenReturn(Optional.of(service));
        when(parts.findByIdAndJobIdForUpdate(20L, 1L)).thenReturn(Optional.of(part));
        when(services.sumSubtotalByJobId(1L)).thenReturn(new BigDecimal("120.00"));
        when(parts.sumSubtotalByJobId(1L)).thenReturn(new BigDecimal("80.00"));

        adapter.deleteService(10L, 1L);
        adapter.deletePart(20L, 1L);

        assertEquals(new BigDecimal("200.00"), adapter.calculateActualSubtotal(1L));
        verify(services).delete(service);
        verify(parts).delete(part);
    }

    private JobServiceLine serviceLine(Long id, String subtotal) {
        return new JobServiceLine(id, 1L, 2L, "Alignment", BigDecimal.ONE,
                new BigDecimal(subtotal), new BigDecimal(subtotal), LocalDateTime.now(),
                id == null ? null : LocalDateTime.now());
    }

    private JobPartLine partLine(Long id, String subtotal) {
        return new JobPartLine(id, 1L, 3L, "Filter", BigDecimal.ONE,
                new BigDecimal(subtotal), new BigDecimal(subtotal), LocalDateTime.now(),
                id == null ? null : LocalDateTime.now());
    }

    private void stubServiceView() {
        when(serviceView.getLineId()).thenReturn(10L);
        when(serviceView.getJobId()).thenReturn(1L);
        when(serviceView.getCatalogId()).thenReturn(2L);
        when(serviceView.getCatalogName()).thenReturn("Alignment");
        when(serviceView.getQuantity()).thenReturn(BigDecimal.ONE);
        when(serviceView.getUnitPrice()).thenReturn(new BigDecimal("120.00"));
        when(serviceView.getLineSubtotal()).thenReturn(new BigDecimal("120.00"));
    }

    private void stubPartView() {
        when(partView.getLineId()).thenReturn(20L);
        when(partView.getJobId()).thenReturn(1L);
        when(partView.getCatalogId()).thenReturn(3L);
        when(partView.getCatalogName()).thenReturn("Filter");
        when(partView.getQuantity()).thenReturn(BigDecimal.ONE);
        when(partView.getUnitPrice()).thenReturn(new BigDecimal("80.00"));
        when(partView.getLineSubtotal()).thenReturn(new BigDecimal("80.00"));
    }
}
