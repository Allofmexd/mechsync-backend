package com.mechsync.shared.web.controller;

import com.mechsync.modules.auth.domain.exception.InvalidCredentialsException;
import com.mechsync.modules.customers.domain.exception.CustomerInUseException;
import com.mechsync.modules.customers.domain.exception.CustomerNotFoundException;
import com.mechsync.modules.customers.domain.exception.CustomerUserNotFoundException;
import com.mechsync.modules.customers.domain.exception.DuplicateCustomerException;
import com.mechsync.modules.catalogs.domain.exception.InvalidStatusContextException;
import com.mechsync.modules.users.domain.exception.DuplicateUserEmailException;
import com.mechsync.modules.users.domain.exception.InvalidUserRoleException;
import com.mechsync.modules.users.domain.exception.RoleNotFoundException;
import com.mechsync.modules.users.domain.exception.SelfRoleChangeNotAllowedException;
import com.mechsync.modules.users.domain.exception.UserNotFoundException;
import com.mechsync.modules.vehicles.domain.exception.DuplicateVehicleException;
import com.mechsync.modules.vehicles.domain.exception.InvalidVehicleYearException;
import com.mechsync.modules.vehicles.domain.exception.VehicleCustomerNotFoundException;
import com.mechsync.modules.vehicles.domain.exception.VehicleInUseException;
import com.mechsync.modules.vehicles.domain.exception.VehicleNotFoundException;
import com.mechsync.modules.vehicleintakes.domain.exception.VehicleIntakeInUseException;
import com.mechsync.modules.vehicleintakes.domain.exception.VehicleIntakeNotFoundException;
import com.mechsync.modules.vehicleintakes.domain.exception.VehicleIntakeStatusNotFoundException;
import com.mechsync.modules.vehicleintakes.domain.exception.VehicleIntakeTechnicianNotFoundException;
import com.mechsync.modules.vehicleintakes.domain.exception.VehicleIntakeVehicleNotFoundException;
import com.mechsync.modules.workorders.domain.exception.InvalidWorkOrderDatesException;
import com.mechsync.modules.workorders.domain.exception.WorkOrderInUseException;
import com.mechsync.modules.workorders.domain.exception.WorkOrderNotFoundException;
import com.mechsync.modules.workorders.domain.exception.WorkOrderStatusNotFoundException;
import com.mechsync.modules.workorders.domain.exception.WorkOrderTechnicianNotFoundException;
import com.mechsync.modules.workorders.domain.exception.WorkOrderVehicleIntakeNotFoundException;
import com.mechsync.modules.workorders.domain.exception.InvalidWorkOrderRevisionException;
import com.mechsync.modules.workorders.domain.exception.WorkOrderRevisionConflictException;
import com.mechsync.modules.workorders.domain.exception.WorkOrderRevisionNotFoundException;
import com.mechsync.shared.web.response.ApiResponse;
import com.mechsync.shared.web.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleInvalidCredentials() {
        return error(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ValidationErrorResponse>> handleValidation(
            MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                fields.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));
        ValidationErrorResponse body = new ValidationErrorResponse(
                "Validation failed", fields, Instant.now());
        return ResponseEntity.badRequest().body(ApiResponse.error(body));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUnreadableBody() {
        return error(HttpStatus.BAD_REQUEST, "Invalid request body");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleAccessDenied() {
        return error(HttpStatus.FORBIDDEN, "Forbidden");
    }

    @ExceptionHandler({
            CustomerNotFoundException.class,
            CustomerUserNotFoundException.class,
            UserNotFoundException.class,
            RoleNotFoundException.class,
            VehicleNotFoundException.class,
            VehicleCustomerNotFoundException.class,
            VehicleIntakeNotFoundException.class,
            VehicleIntakeVehicleNotFoundException.class,
            VehicleIntakeTechnicianNotFoundException.class,
            VehicleIntakeStatusNotFoundException.class,
            WorkOrderNotFoundException.class,
            WorkOrderVehicleIntakeNotFoundException.class,
            WorkOrderTechnicianNotFoundException.class,
            WorkOrderStatusNotFoundException.class,
            WorkOrderRevisionNotFoundException.class
    })
    public ResponseEntity<ApiResponse<ErrorResponse>> handleNotFound(RuntimeException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({
            DuplicateCustomerException.class,
            CustomerInUseException.class,
            DuplicateUserEmailException.class,
            SelfRoleChangeNotAllowedException.class,
            DuplicateVehicleException.class,
            VehicleInUseException.class,
            VehicleIntakeInUseException.class,
            WorkOrderInUseException.class,
            WorkOrderRevisionConflictException.class
    })
    public ResponseEntity<ApiResponse<ErrorResponse>> handleConflict(RuntimeException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(InvalidUserRoleException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleInvalidRole(
            InvalidUserRoleException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(InvalidStatusContextException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleInvalidStatusContext(
            InvalidStatusContextException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(InvalidVehicleYearException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleInvalidVehicleYear(
            InvalidVehicleYearException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(InvalidWorkOrderDatesException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleInvalidWorkOrderDates(
            InvalidWorkOrderDatesException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(InvalidWorkOrderRevisionException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleInvalidWorkOrderRevision(
            InvalidWorkOrderRevisionException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMethodValidation() {
        return error(HttpStatus.BAD_REQUEST, "Validation failed");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleNoResourceFound() {
        return error(HttpStatus.NOT_FOUND, "Not found");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUnexpected(Exception exception) {
        LOGGER.error("Unexpected request failure", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }

    private ResponseEntity<ApiResponse<ErrorResponse>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(new ErrorResponse(message, Instant.now())));
    }

    public record ValidationErrorResponse(
            String message,
            Map<String, String> fields,
            Instant timestamp) {
    }
}
