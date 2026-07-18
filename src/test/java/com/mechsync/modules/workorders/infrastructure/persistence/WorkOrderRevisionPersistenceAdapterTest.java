package com.mechsync.modules.workorders.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mechsync.modules.workorders.domain.model.*;
import com.mechsync.modules.workorders.infrastructure.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WorkOrderRevisionPersistenceAdapterTest {
 @Mock WorkOrderJpaRepository workOrders;@Mock WorkOrderRevisionJpaRepository revisions;
 @Mock WorkOrderRevisionServiceJpaRepository services;@Mock WorkOrderRevisionPartJpaRepository parts;
 @Mock WorkOrderRevisionStatusJpaRepository statuses;@Mock WorkOrderAcceptanceMethodJpaRepository methods;
 WorkOrderRevisionPersistenceAdapter adapter;

 @BeforeEach void setUp(){adapter=new WorkOrderRevisionPersistenceAdapter(
  workOrders,revisions,services,parts,statuses,methods);}

 @Test void loadsScopedRevisionWithSnapshotLinesAndBigDecimalMappings(){
  WorkOrderJpaEntity parent=parent();parent.setCurrentRevisionId(7L);
  WorkOrderRevisionJpaEntity entity=revisionEntity();ReflectionTestUtils.setField(entity,"id",7L);
  when(revisions.findByIdAndWorkOrderId(7L,1L)).thenReturn(Optional.of(entity));
  when(workOrders.findById(1L)).thenReturn(Optional.of(parent));when(statuses.findAll()).thenReturn(List.of(status(1L,"DRAFT")));
  when(methods.findAll()).thenReturn(List.of());
  when(services.findByRevisionIdOrderByLineNumber(7L)).thenReturn(List.of(
   new WorkOrderRevisionServiceJpaEntity(7L,1,null,"Custom service",null,new BigDecimal("1.000000"),
    new BigDecimal("10.0000"),new BigDecimal("10.0000"),null)));
  when(parts.findByRevisionIdOrderByLineNumber(7L)).thenReturn(List.of());

  WorkOrderRevision result=adapter.findById(1L,7L,true).orElseThrow();

  assertTrue(result.current());assertEquals(new BigDecimal("100.00"),result.subtotalAmount());
  assertEquals("Custom service",result.services().get(0).nameSnapshot());
  verify(revisions).findByIdAndWorkOrderId(7L,1L);
 }

 @Test void insertsHeaderAndLinesWithoutExposingJpaEntities(){
  WorkOrderRevisionStatusJpaEntity draft=status(1L,"DRAFT");when(statuses.findByCode("DRAFT")).thenReturn(Optional.of(draft));
  when(statuses.findAll()).thenReturn(List.of(draft));when(methods.findAll()).thenReturn(List.of());
  when(revisions.saveAndFlush(any())).thenAnswer(invocation->{WorkOrderRevisionJpaEntity value=invocation.getArgument(0);
   ReflectionTestUtils.setField(value,"id",9L);return value;});
  when(services.saveAllAndFlush(any())).thenAnswer(invocation->invocation.getArgument(0));
  when(parts.saveAllAndFlush(any())).thenAnswer(invocation->invocation.getArgument(0));
  when(workOrders.findById(1L)).thenReturn(Optional.of(parent()));

  WorkOrderRevision result=adapter.insert(domainRevision());

  assertEquals(9L,result.id());assertEquals(1,result.services().size());
  verify(revisions).saveAndFlush(any(WorkOrderRevisionJpaEntity.class));verify(services).saveAllAndFlush(any());
 }

 @Test void resolvesCurrentAndFinalPointersWithoutCrossWorkOrderLookup(){
  WorkOrderJpaEntity parent=parent();parent.setCurrentRevisionId(7L);parent.setFinalApprovedRevisionId(8L);
  WorkOrderRevisionJpaEntity current=revisionEntity();ReflectionTestUtils.setField(current,"id",7L);
  when(workOrders.findById(1L)).thenReturn(Optional.of(parent));
  when(revisions.findByIdAndWorkOrderId(7L,1L)).thenReturn(Optional.of(current));
  when(statuses.findAll()).thenReturn(List.of(status(1L,"DRAFT")));when(methods.findAll()).thenReturn(List.of());
  assertTrue(adapter.findCurrent(1L,false).orElseThrow().current());
  verify(revisions).findByIdAndWorkOrderId(7L,1L);
 }

 private WorkOrderJpaEntity parent(){return new WorkOrderJpaEntity(1L,2L,3L,LocalDateTime.now(),null,null,
  BigDecimal.ONE,new BigDecimal("100.00"),new BigDecimal("16.00"),new BigDecimal("116.00"),null,4L,
  LocalDateTime.now(),null);}
 private WorkOrderRevisionJpaEntity revisionEntity(){return new WorkOrderRevisionJpaEntity(1L,1,1L,3L,null,null,
  new BigDecimal("1.0000"),new BigDecimal("100.00"),true,new BigDecimal("0.160000"),
  new BigDecimal("16.00"),new BigDecimal("116.00"),"MXN",null,"Notes",null,null,99L);}
 private WorkOrderRevisionStatusJpaEntity status(Long id,String code){WorkOrderRevisionStatusJpaEntity value=
  new WorkOrderRevisionStatusJpaEntity();ReflectionTestUtils.setField(value,"id",id);
  ReflectionTestUtils.setField(value,"code",code);return value;}
 private WorkOrderRevision domainRevision(){return new WorkOrderRevision(null,1L,1,WorkOrderRevisionStatus.DRAFT,3L,
  null,null,new BigDecimal("1.0000"),new BigDecimal("100.00"),true,new BigDecimal("0.160000"),
  new BigDecimal("16.00"),new BigDecimal("116.00"),"MXN",null,"Notes",null,null,99L,null,null,null,null,null,
  null,null,0,null,null,false,false,List.of(new WorkOrderRevisionServiceLine(null,null,1,null,"Custom service",null,
   new BigDecimal("1.000000"),new BigDecimal("10.0000"),new BigDecimal("10.0000"),null,null)),List.of());}
}
