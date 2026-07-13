package com.mechsync.modules.customers.web.controller;

import com.mechsync.modules.customers.application.dto.CreateCustomerCommand;
import com.mechsync.modules.customers.application.dto.UpdateCustomerCommand;
import com.mechsync.modules.customers.application.port.in.CreateCustomerUseCase;
import com.mechsync.modules.customers.application.port.in.DeleteCustomerUseCase;
import com.mechsync.modules.customers.application.port.in.GetCustomerByIdUseCase;
import com.mechsync.modules.customers.application.port.in.ListCustomersUseCase;
import com.mechsync.modules.customers.application.port.in.UpdateCustomerUseCase;
import com.mechsync.modules.customers.domain.model.Customer;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiPaths.CUSTOMERS)
public class CustomerController {

    private final ListCustomersUseCase listCustomersUseCase;
    private final GetCustomerByIdUseCase getCustomerByIdUseCase;
    private final CreateCustomerUseCase createCustomerUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final DeleteCustomerUseCase deleteCustomerUseCase;

    public CustomerController(
            ListCustomersUseCase listCustomersUseCase,
            GetCustomerByIdUseCase getCustomerByIdUseCase,
            CreateCustomerUseCase createCustomerUseCase,
            UpdateCustomerUseCase updateCustomerUseCase,
            DeleteCustomerUseCase deleteCustomerUseCase) {
        this.listCustomersUseCase = listCustomersUseCase;
        this.getCustomerByIdUseCase = getCustomerByIdUseCase;
        this.createCustomerUseCase = createCustomerUseCase;
        this.updateCustomerUseCase = updateCustomerUseCase;
        this.deleteCustomerUseCase = deleteCustomerUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<CustomerPageResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.ok(CustomerPageResponse.from(listCustomersUseCase.list(page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<CustomerResponse> getById(@PathVariable @Positive Long id) {
        return ApiResponse.ok(CustomerResponse.from(getCustomerByIdUseCase.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<CustomerResponse>> create(
            @Valid @RequestBody CreateCustomerRequest request) {
        Customer created = createCustomerUseCase.create(
                new CreateCustomerCommand(request.userId(), request.address()));
        return ResponseEntity.created(URI.create(ApiPaths.CUSTOMERS + "/" + created.id()))
                .body(ApiResponse.ok(CustomerResponse.from(created)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<CustomerResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        Customer updated = updateCustomerUseCase.update(
                new UpdateCustomerCommand(id, request.address()));
        return ApiResponse.ok(CustomerResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        deleteCustomerUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
