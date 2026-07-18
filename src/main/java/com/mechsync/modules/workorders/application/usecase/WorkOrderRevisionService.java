package com.mechsync.modules.workorders.application.usecase;

import com.mechsync.modules.workorders.application.dto.*;
import com.mechsync.modules.workorders.application.port.in.*;
import com.mechsync.modules.workorders.application.port.out.CatalogSnapshot;
import com.mechsync.modules.workorders.application.port.out.WorkOrderRevisionParent;
import com.mechsync.modules.workorders.application.port.out.WorkOrderRevisionRepositoryPort;
import com.mechsync.modules.workorders.domain.exception.*;
import com.mechsync.modules.workorders.domain.model.*;
import com.mechsync.modules.workorders.domain.service.WorkOrderRevisionMoneyCalculator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WorkOrderRevisionService implements
        WorkOrderRevisionQueryUseCase,
        CreateWorkOrderRevisionUseCase,
        WorkOrderRevisionWorkflowUseCase {

    private static final String DEFAULT_CURRENCY = "MXN";

    private final WorkOrderRevisionRepositoryPort repository;
    private final WorkOrderRevisionMoneyCalculator calculator = new WorkOrderRevisionMoneyCalculator();

    public WorkOrderRevisionService(WorkOrderRevisionRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public WorkOrderRevisionPage list(Long workOrderId, int page, int size, RevisionActor actor) {
        authorizeRead(workOrderId, actor);
        return repository.findAll(workOrderId, page, size);
    }

    @Override
    public WorkOrderRevision get(Long workOrderId, Long revisionId, RevisionActor actor) {
        authorizeRead(workOrderId, actor);
        return requireRevision(workOrderId, revisionId, true);
    }

    @Override
    public WorkOrderRevision getCurrent(Long workOrderId, RevisionActor actor) {
        authorizeRead(workOrderId, actor);
        return repository.findCurrent(workOrderId, true)
                .orElseThrow(() -> new WorkOrderRevisionNotFoundException(workOrderId, null));
    }

    @Override
    public WorkOrderRevision getFinalApproved(Long workOrderId, RevisionActor actor) {
        authorizeRead(workOrderId, actor);
        return repository.findFinalApproved(workOrderId, true)
                .orElseThrow(() -> new WorkOrderRevisionNotFoundException(workOrderId, null));
    }

    @Override
    @Transactional
    public WorkOrderRevision create(CreateWorkOrderRevisionCommand command) {
        requireAdministrator(command.actor());
        WorkOrderRevisionParent parent = lockParent(command.workOrderId());
        WorkOrderRevision previous = parent.currentRevisionId() == null
                ? null
                : requireRevision(command.workOrderId(), parent.currentRevisionId(), false);
        if (previous != null
                && (previous.status() == WorkOrderRevisionStatus.CANCELLED
                || previous.status() == WorkOrderRevisionStatus.SUPERSEDED)) {
            throw new WorkOrderRevisionConflictException(
                    "A cancelled or superseded current revision cannot be replaced");
        }
        if (!repository.technicianExists(command.technicianId())) {
            throw new WorkOrderTechnicianNotFoundException(command.technicianId());
        }
        validateDates(command.estimatedStartDate(), command.estimatedDeliveryDate());
        validateHours(command.estimatedHours());
        int revisionNumber = repository.nextRevisionNumber(command.workOrderId());
        String changeReason = trim(command.changeReason());
        if (revisionNumber > 1 && changeReason == null) {
            throw new InvalidWorkOrderRevisionException(
                    "changeReason is required from revision 2 onward");
        }
        String currency = command.currency() == null
                ? DEFAULT_CURRENCY
                : command.currency().trim().toUpperCase(Locale.ROOT);
        if (!DEFAULT_CURRENCY.equals(currency)) {
            throw new InvalidWorkOrderRevisionException("Only MXN is supported in the MVP");
        }
        List<WorkOrderRevisionServiceLine> services = buildServices(command.services());
        List<WorkOrderRevisionPartLine> parts = buildParts(command.parts());
        List<BigDecimal> lineSubtotals = new ArrayList<>();
        services.forEach(line -> lineSubtotals.add(line.lineSubtotal()));
        parts.forEach(line -> lineSubtotals.add(line.lineSubtotal()));
        QuotationAmounts amounts = calculator.calculateFromLines(
                lineSubtotals,
                command.subtotalAmount(),
                command.applyIva(),
                command.ivaRate(),
                command.ivaAmount(),
                command.totalAmount());
        WorkOrderRevision inserted = repository.insert(new WorkOrderRevision(
                null,
                command.workOrderId(),
                revisionNumber,
                WorkOrderRevisionStatus.DRAFT,
                command.technicianId(),
                command.estimatedStartDate(),
                command.estimatedDeliveryDate(),
                command.estimatedHours(),
                amounts.subtotalAmount(),
                amounts.applyIva(),
                amounts.ivaRate(),
                amounts.ivaAmount(),
                amounts.totalAmount(),
                currency,
                trim(command.taxTreatmentNotes()),
                trim(command.technicalObservations()),
                trim(command.customerNotes()),
                changeReason,
                command.actor().userId(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                null,
                null,
                false,
                false,
                services,
                parts));
        repository.setCurrentRevision(command.workOrderId(), inserted.id());
        if (previous != null && previous.status().isSupersededWhenReplaced()) {
            repository.transition(
                    command.workOrderId(),
                    previous.id(),
                    WorkOrderRevisionStatus.SUPERSEDED,
                    LocalDateTime.now());
        }
        return requireRevision(command.workOrderId(), inserted.id(), true);
    }

    @Override
    @Transactional
    public WorkOrderRevision send(Long workOrderId, Long revisionId, RevisionActor actor) {
        return transitionCurrent(workOrderId, revisionId, actor, WorkOrderRevisionStatus.SENT);
    }

    @Override
    @Transactional
    public WorkOrderRevision approve(ApproveWorkOrderRevisionCommand command) {
        requireAdministrator(command.actor());
        WorkOrderRevisionParent parent = lockParent(command.workOrderId());
        requireCurrent(parent, command.revisionId());
        WorkOrderRevision revision = requireRevision(command.workOrderId(), command.revisionId(), false);
        requireTransition(revision, WorkOrderRevisionStatus.APPROVED);
        String acceptedByName = trim(command.acceptedByName());
        if (acceptedByName == null) {
            throw new InvalidWorkOrderRevisionException("acceptedByName is required for approval");
        }
        if (command.acceptedByUserId() != null
                && !repository.userExists(command.acceptedByUserId())) {
            throw new InvalidWorkOrderRevisionException("acceptedByUserId does not exist");
        }
        String method = trim(command.acceptanceMethod());
        if (method == null) {
            throw new InvalidWorkOrderRevisionException("acceptanceMethod is required for approval");
        }
        method = method.toUpperCase(Locale.ROOT);
        if (!repository.acceptanceMethodExists(method)) {
            throw new InvalidWorkOrderRevisionException("Invalid acceptanceMethod");
        }
        String acceptanceNotes = trim(command.acceptanceNotes());
        if ("OTHER".equals(method) && acceptanceNotes == null) {
            throw new InvalidWorkOrderRevisionException(
                    "acceptanceNotes is required when acceptanceMethod is OTHER");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime acceptedAt = command.acceptedAt() == null ? now : command.acceptedAt();
        if (acceptedAt.isAfter(now.plusMinutes(5))) {
            throw new InvalidWorkOrderRevisionException("acceptedAt cannot be in the future");
        }
        repository.approve(
                command.workOrderId(),
                command.revisionId(),
                command.actor().userId(),
                now,
                acceptedByName,
                command.acceptedByUserId(),
                acceptedAt,
                method,
                acceptanceNotes);
        repository.setFinalApprovedRevision(command.workOrderId(), command.revisionId());
        return requireRevision(command.workOrderId(), command.revisionId(), true);
    }

    @Override
    @Transactional
    public WorkOrderRevision reject(Long workOrderId, Long revisionId, RevisionActor actor) {
        return transitionCurrent(workOrderId, revisionId, actor, WorkOrderRevisionStatus.REJECTED);
    }

    @Override
    @Transactional
    public WorkOrderRevision cancel(Long workOrderId, Long revisionId, RevisionActor actor) {
        return transitionCurrent(workOrderId, revisionId, actor, WorkOrderRevisionStatus.CANCELLED);
    }

    private WorkOrderRevision transitionCurrent(
            Long workOrderId,
            Long revisionId,
            RevisionActor actor,
            WorkOrderRevisionStatus target) {
        requireAdministrator(actor);
        WorkOrderRevisionParent parent = lockParent(workOrderId);
        requireCurrent(parent, revisionId);
        WorkOrderRevision revision = requireRevision(workOrderId, revisionId, false);
        requireTransition(revision, target);
        repository.transition(workOrderId, revisionId, target, LocalDateTime.now());
        return requireRevision(workOrderId, revisionId, true);
    }

    private void requireTransition(WorkOrderRevision revision, WorkOrderRevisionStatus target) {
        if (!revision.status().canTransitionTo(target)) {
            throw new WorkOrderRevisionConflictException(
                    "Invalid revision transition: " + revision.status() + " -> " + target);
        }
    }

    private void requireCurrent(WorkOrderRevisionParent parent, Long revisionId) {
        if (!Objects.equals(parent.currentRevisionId(), revisionId)) {
            throw new WorkOrderRevisionConflictException("Only the current revision can be transitioned");
        }
    }

    private WorkOrderRevisionParent lockParent(Long workOrderId) {
        return repository.lockWorkOrder(workOrderId)
                .orElseThrow(() -> new WorkOrderNotFoundException(workOrderId));
    }

    private WorkOrderRevision requireRevision(Long workOrderId, Long revisionId, boolean withLines) {
        return repository.findById(workOrderId, revisionId, withLines)
                .orElseThrow(() -> new WorkOrderRevisionNotFoundException(workOrderId, revisionId));
    }

    private void authorizeRead(Long workOrderId, RevisionActor actor) {
        if (!repository.workOrderExists(workOrderId)) {
            throw new WorkOrderNotFoundException(workOrderId);
        }
        if (actor.isAdministrator()) {
            return;
        }
        if (actor.isTechnician()
                && repository.isAssignedToTechnicianUser(workOrderId, actor.userId())) {
            return;
        }
        throw new WorkOrderRevisionNotFoundException(workOrderId, null);
    }

    private void requireAdministrator(RevisionActor actor) {
        if (!actor.isAdministrator()) {
            throw new AccessDeniedException("Forbidden");
        }
    }

    private List<WorkOrderRevisionServiceLine> buildServices(
            List<CreateRevisionServiceLineCommand> commands) {
        List<CreateRevisionServiceLineCommand> safe = commands == null ? List.of() : commands;
        ensureUniqueLineNumbers(safe.stream().map(CreateRevisionServiceLineCommand::lineNumber).toList());
        List<WorkOrderRevisionServiceLine> result = new ArrayList<>();
        for (CreateRevisionServiceLineCommand command : safe) {
            CatalogSnapshot snapshot = command.serviceId() == null
                    ? customSnapshot(command.nameSnapshot(), command.descriptionSnapshot())
                    : repository.findServiceSnapshot(command.serviceId())
                            .orElseThrow(() -> new InvalidWorkOrderRevisionException(
                                    "Service not found: " + command.serviceId()));
            BigDecimal lineSubtotal = calculator.calculateLineSubtotal(
                    command.quantity(), command.unitPrice(), command.lineSubtotal());
            result.add(new WorkOrderRevisionServiceLine(
                    null, null, command.lineNumber(), command.serviceId(), snapshot.name(),
                    snapshot.description(), command.quantity(), command.unitPrice(), lineSubtotal,
                    trim(command.notes()), null));
        }
        return result;
    }

    private List<WorkOrderRevisionPartLine> buildParts(List<CreateRevisionPartLineCommand> commands) {
        List<CreateRevisionPartLineCommand> safe = commands == null ? List.of() : commands;
        ensureUniqueLineNumbers(safe.stream().map(CreateRevisionPartLineCommand::lineNumber).toList());
        List<WorkOrderRevisionPartLine> result = new ArrayList<>();
        for (CreateRevisionPartLineCommand command : safe) {
            CatalogSnapshot snapshot = command.partId() == null
                    ? customSnapshot(command.nameSnapshot(), command.descriptionSnapshot())
                    : repository.findPartSnapshot(command.partId())
                            .orElseThrow(() -> new InvalidWorkOrderRevisionException(
                                    "Part not found: " + command.partId()));
            BigDecimal lineSubtotal = calculator.calculateLineSubtotal(
                    command.quantity(), command.unitPrice(), command.lineSubtotal());
            result.add(new WorkOrderRevisionPartLine(
                    null, null, command.lineNumber(), command.partId(), snapshot.name(),
                    snapshot.reference(), snapshot.description(), command.quantity(), command.unitPrice(),
                    lineSubtotal, trim(command.notes()), null));
        }
        return result;
    }

    private CatalogSnapshot customSnapshot(String name, String description) {
        String normalizedName = trim(name);
        if (normalizedName == null) {
            throw new InvalidWorkOrderRevisionException(
                    "nameSnapshot is required when no catalog id is provided");
        }
        return new CatalogSnapshot(normalizedName, trim(description), null);
    }

    private void ensureUniqueLineNumbers(List<Integer> lineNumbers) {
        Set<Integer> unique = new HashSet<>();
        for (Integer lineNumber : lineNumbers) {
            if (lineNumber == null || lineNumber <= 0 || !unique.add(lineNumber)) {
                throw new InvalidWorkOrderRevisionException(
                        "Line numbers must be positive and unique within each collection");
            }
        }
    }

    private void validateDates(LocalDateTime start, LocalDateTime delivery) {
        if (start != null && delivery != null && delivery.isBefore(start)) {
            throw new InvalidWorkOrderRevisionException(
                    "estimatedDeliveryDate cannot be before estimatedStartDate");
        }
    }

    private void validateHours(BigDecimal hours) {
        if (hours != null && (hours.signum() < 0 || hours.scale() > 4)) {
            throw new InvalidWorkOrderRevisionException(
                    "estimatedHours must be non-negative and have at most 4 decimal places");
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
