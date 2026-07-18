package com.mechsync.modules.workorders.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mechsync.modules.workorders.application.dto.*;
import com.mechsync.modules.workorders.application.port.out.*;
import com.mechsync.modules.workorders.domain.exception.*;
import com.mechsync.modules.workorders.domain.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkOrderRevisionServiceTest {
    @Mock WorkOrderRevisionRepositoryPort repository;
    WorkOrderRevisionService service;

    @BeforeEach void setUp() { service = new WorkOrderRevisionService(repository); }

    @Test
    void createsSuccessorAndSupersedesPreviousCurrentRevision() {
        WorkOrderRevision previous = revision(10L, WorkOrderRevisionStatus.DRAFT, true, false);
        WorkOrderRevision created = revision(11L, WorkOrderRevisionStatus.DRAFT, true, false);
        when(repository.lockWorkOrder(1L)).thenReturn(Optional.of(new WorkOrderRevisionParent(1L,10L,null,0)));
        when(repository.findById(1L,10L,false)).thenReturn(Optional.of(previous));
        when(repository.technicianExists(2L)).thenReturn(true);
        when(repository.nextRevisionNumber(1L)).thenReturn(2);
        when(repository.insert(any())).thenReturn(created);
        when(repository.findById(1L,11L,true)).thenReturn(Optional.of(created));

        WorkOrderRevision result = service.create(createCommand("Updated quote"));

        assertEquals(11L,result.id());
        InOrder order=inOrder(repository);
        order.verify(repository).setCurrentRevision(1L,11L);
        order.verify(repository).transition(eq(1L),eq(10L),eq(WorkOrderRevisionStatus.SUPERSEDED),any());
    }

    @Test
    void rejectsApprovalWithoutAcceptedName() {
        stubCurrent(WorkOrderRevisionStatus.SENT);
        assertThrows(InvalidWorkOrderRevisionException.class, () -> service.approve(
                new ApproveWorkOrderRevisionCommand(1L,10L,admin(),"   ",null,null,"IN_PERSON",null)));
        verify(repository,never()).approve(anyLong(),anyLong(),anyLong(),any(),any(),any(),any(),any(),any());
    }

    @Test
    void approvesWithOptionalAcceptedUser() {
        stubCurrent(WorkOrderRevisionStatus.SENT);
        WorkOrderRevision approved=revision(10L,WorkOrderRevisionStatus.APPROVED,true,true);
        when(repository.acceptanceMethodExists("IN_PERSON")).thenReturn(true);
        when(repository.findById(1L,10L,true)).thenReturn(Optional.of(approved));

        WorkOrderRevision result=service.approve(new ApproveWorkOrderRevisionCommand(
                1L,10L,admin(),"Customer Name",null,null,"in_person",null));

        assertEquals(WorkOrderRevisionStatus.APPROVED,result.status());
        verify(repository).approve(eq(1L),eq(10L),eq(99L),any(),eq("Customer Name"),isNull(),any(),
                eq("IN_PERSON"),isNull());
        verify(repository).setFinalApprovedRevision(1L,10L);
    }

    @Test
    void rejectsInvalidTransition() {
        stubCurrent(WorkOrderRevisionStatus.APPROVED);
        assertThrows(WorkOrderRevisionConflictException.class, () -> service.send(1L,10L,admin()));
    }

    @Test
    void technicianCanOnlyReadAssignedWorkOrder() {
        when(repository.workOrderExists(1L)).thenReturn(true);
        when(repository.isAssignedToTechnicianUser(1L,7L)).thenReturn(true);
        when(repository.findAll(1L,0,20)).thenReturn(new WorkOrderRevisionPage(List.of(),0,20,0,0));
        assertDoesNotThrow(() -> service.list(1L,0,20,new RevisionActor(7L,Set.of("TECNICO"))));

        when(repository.isAssignedToTechnicianUser(1L,8L)).thenReturn(false);
        assertThrows(WorkOrderRevisionNotFoundException.class, () ->
                service.list(1L,0,20,new RevisionActor(8L,Set.of("TECNICO"))));
    }

    private void stubCurrent(WorkOrderRevisionStatus status){
        when(repository.lockWorkOrder(1L)).thenReturn(Optional.of(new WorkOrderRevisionParent(1L,10L,null,0)));
        when(repository.findById(1L,10L,false)).thenReturn(Optional.of(revision(10L,status,true,false)));
    }
    private RevisionActor admin(){return new RevisionActor(99L,Set.of("ADMINISTRADOR"));}
    private CreateWorkOrderRevisionCommand createCommand(String reason){return new CreateWorkOrderRevisionCommand(
            1L,admin(),2L,null,null,new BigDecimal("1.0000"),"MXN",true,null,
            new BigDecimal("100.00"),new BigDecimal("16.00"),new BigDecimal("116.00"),null,
            "Notes",null,reason,List.of(new CreateRevisionServiceLineCommand(
                    1,null,"Custom service",null,BigDecimal.ONE,new BigDecimal("100.0000"),
                    new BigDecimal("100.0000"),null)),List.of());}
    private WorkOrderRevision revision(Long id,WorkOrderRevisionStatus status,boolean current,boolean approved){
        return new WorkOrderRevision(id,1L,id.intValue()==10?1:2,status,2L,null,null,new BigDecimal("1.0000"),
                new BigDecimal("100.00"),true,new BigDecimal("0.160000"),new BigDecimal("16.00"),
                new BigDecimal("116.00"),"MXN",null,"Notes",null,id.intValue()==10?null:"Reason",99L,
                approved?99L:null,approved?LocalDateTime.now():null,approved?"Customer":null,null,
                approved?LocalDateTime.now():null,approved?"IN_PERSON":null,null,0,LocalDateTime.now(),null,
                current,approved,List.of(),List.of());}
}
