package com.mechsync.modules.customerportal.web.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.auth.infrastructure.security.JwtService;
import com.mechsync.modules.auth.infrastructure.security.SpringSecurityCurrentAuthenticatedUserAdapter;
import com.mechsync.modules.customerportal.application.port.out.CustomerPortalQueryPort;
import com.mechsync.modules.customerportal.application.port.out.CustomerPortalHistoryQueryPort;
import com.mechsync.modules.customerportal.application.port.out.CustomerPortalIntakeQueryPort;
import com.mechsync.modules.customerportal.application.port.out.CustomerPortalJobQueryPort;
import com.mechsync.modules.customerportal.application.port.out.CustomerPortalWorkOrderQueryPort;
import com.mechsync.modules.customerportal.application.usecase.CustomerPortalOperationsService;
import com.mechsync.modules.customerportal.application.usecase.CustomerPortalService;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalProfile;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalHistoryEvent;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalIntakeDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalIntakeSummary;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalJobDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalPage;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalQuotation;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalVehicleInfo;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalWorkOrderDetail;
import com.mechsync.modules.customers.application.port.out.CustomerRepositoryPort;
import com.mechsync.modules.customers.application.usecase.AuthenticatedCustomerService;
import com.mechsync.modules.customers.domain.model.Customer;
import com.mechsync.modules.vehicles.application.dto.VehiclePage;
import com.mechsync.modules.vehicles.domain.model.Vehicle;
import com.mechsync.shared.infrastructure.config.SecurityConfig;
import com.mechsync.shared.infrastructure.security.JwtAuthenticationFilter;
import com.mechsync.shared.infrastructure.security.RestAccessDeniedHandler;
import com.mechsync.shared.infrastructure.security.RestAuthenticationEntryPoint;
import com.mechsync.shared.web.controller.GlobalExceptionHandler;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = CustomerPortalController.class, properties = {
        "debug=false",
        "mechsync.security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "mechsync.security.jwt.expiration-minutes=120",
        "mechsync.security.jwt.issuer=mechsync-backend"
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class,
        SpringSecurityCurrentAuthenticatedUserAdapter.class,
        AuthenticatedCustomerService.class,
        CustomerPortalService.class,
        CustomerPortalOperationsService.class
})
class CustomerPortalControllerTest {

    private static final String PROFILE = "/api/v1/customer-portal/profile";
    private static final String VEHICLES = "/api/v1/customer-portal/vehicles";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomerRepositoryPort customerRepository;

    @MockitoBean
    private CustomerPortalQueryPort queryPort;

    @MockitoBean
    private CustomerPortalIntakeQueryPort intakeQueryPort;

    @MockitoBean
    private CustomerPortalWorkOrderQueryPort workOrderQueryPort;

    @MockitoBean
    private CustomerPortalJobQueryPort jobQueryPort;

    @MockitoBean
    private CustomerPortalHistoryQueryPort historyQueryPort;

    @Test
    void customerCanReadSafeProfile() throws Exception {
        allowCustomer("customer-token", 7L, 3L);
        when(queryPort.findProfileByCustomerId(3L)).thenReturn(Optional.of(new CustomerPortalProfile(
                3L, "Ana", "Cliente", "ana@example.com", "9610000000", "Dirección")));

        mockMvc.perform(authenticatedGet(PROFILE, "customer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerId", is(3)))
                .andExpect(jsonPath("$.data.email", is("ana@example.com")))
                .andExpect(jsonPath("$.data.userId").doesNotExist())
                .andExpect(jsonPath("$.data.roles").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    void customerWithoutProfileReceivesForbidden() throws Exception {
        tokenRepresents("customer-token", 7L, "CLIENTE");
        when(customerRepository.findByUserId(7L)).thenReturn(Optional.empty());

        for (String path : allPortalPaths()) {
            mockMvc.perform(authenticatedGet(path, "customer-token"))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(queryPort);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMINISTRADOR", "TECNICO"})
    void nonCustomerRolesCannotUsePortal(String role) throws Exception {
        tokenRepresents("other-token", 1L, role);

        for (String path : allPortalPaths()) {
            mockMvc.perform(authenticatedGet(path, "other-token"))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(queryPort);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/v1/customer-portal/profile",
            "/api/v1/customer-portal/vehicles",
            "/api/v1/customer-portal/vehicles/8"
    })
    void anonymousRequestsRemainUnauthorized(String path) throws Exception {
        mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
    }

    @Test
    void listReturnsOnlyResolvedCustomerPageWithMaskedVin() throws Exception {
        allowCustomer("customer-token", 7L, 3L);
        when(queryPort.findVehiclesByCustomerId(3L, 0, 5)).thenReturn(new VehiclePage(
                List.of(vehicle(8L, 3L)), 0, 5, 1, 1));

        mockMvc.perform(authenticatedGet(VEHICLES + "?page=0&size=5", "customer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(1)))
                .andExpect(jsonPath("$.data.content[0].vehicleId", is(8)))
                .andExpect(jsonPath("$.data.content[0].maskedVin", is("*************1234")))
                .andExpect(jsonPath("$.data.content[0].vin").doesNotExist());
        verify(queryPort).findVehiclesByCustomerId(3L, 0, 5);
    }

    @Test
    void differentCustomersCannotMixVehicleQueries() throws Exception {
        allowCustomer("customer-a", 7L, 3L);
        allowCustomer("customer-b", 8L, 4L);
        when(queryPort.findVehiclesByCustomerId(3L, 0, 20))
                .thenReturn(new VehiclePage(List.of(vehicle(8L, 3L)), 0, 20, 1, 1));
        when(queryPort.findVehiclesByCustomerId(4L, 0, 20))
                .thenReturn(new VehiclePage(List.of(vehicle(9L, 4L)), 0, 20, 1, 1));

        mockMvc.perform(authenticatedGet(VEHICLES, "customer-a"))
                .andExpect(jsonPath("$.data.content[0].vehicleId", is(8)));
        mockMvc.perform(authenticatedGet(VEHICLES, "customer-b"))
                .andExpect(jsonPath("$.data.content[0].vehicleId", is(9)));
    }

    @Test
    void emptyPageIsSuccessfulAndCustomerScoped() throws Exception {
        allowCustomer("customer-token", 7L, 3L);
        when(queryPort.findVehiclesByCustomerId(3L, 0, 20))
                .thenReturn(new VehiclePage(List.of(), 0, 20, 0, 0));

        mockMvc.perform(authenticatedGet(VEHICLES, "customer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()", is(0)))
                .andExpect(jsonPath("$.data.totalElements", is(0)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"?page=-1", "?size=0", "?size=101"})
    void invalidPaginationReturnsBadRequest(String query) throws Exception {
        allowCustomer("customer-token", 7L, 3L);

        mockMvc.perform(authenticatedGet(VEHICLES + query, "customer-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ownedVehicleDetailContainsFullVin() throws Exception {
        allowCustomer("customer-token", 7L, 3L);
        when(queryPort.findVehicleByIdAndCustomerId(8L, 3L))
                .thenReturn(Optional.of(vehicle(8L, 3L)));

        mockMvc.perform(authenticatedGet(VEHICLES + "/8", "customer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.vehicleId", is(8)))
                .andExpect(jsonPath("$.data.vin", is("1HGCM82633A001234")))
                .andExpect(jsonPath("$.data.customerId").doesNotExist());
    }

    @Test
    void foreignVehicleAndMissingVehicleShareNotFoundResponse() throws Exception {
        allowCustomer("customer-token", 7L, 3L);
        when(queryPort.findVehicleByIdAndCustomerId(99L, 3L)).thenReturn(Optional.empty());

        mockMvc.perform(authenticatedGet(VEHICLES + "/99", "customer-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.message", is("Vehicle not found")));
    }

    @Test
    void invalidVehicleIdReturnsBadRequest() throws Exception {
        allowCustomer("customer-token", 7L, 3L);

        mockMvc.perform(authenticatedGet(VEHICLES + "/0", "customer-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonNumericVehicleIdReturnsGenericBadRequest() throws Exception {
        allowCustomer("customer-token", 7L, 3L);

        mockMvc.perform(authenticatedGet(VEHICLES + "/not-a-number", "customer-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.message", is("Invalid request parameter")));
    }

    @Test
    void customerCanReadOwnPaginatedIntakesWithoutInternalFields() throws Exception {
        allowCustomer("customer-token", 7L, 3L);
        LocalDateTime date = LocalDateTime.of(2026, 7, 20, 10, 30);
        when(intakeQueryPort.findIntakes(3L, 0, 5, null)).thenReturn(new CustomerPortalPage<>(
                List.of(new CustomerPortalIntakeSummary(
                        12L, 8L, "Honda Accord 2022", date, 45000,
                        "Ruido al frenar", "En diagnÃ³stico", date)),
                0, 5, 1, 1));

        mockMvc.perform(authenticatedGet(
                        "/api/v1/customer-portal/vehicle-intakes?page=0&size=5",
                        "customer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(1)))
                .andExpect(jsonPath("$.data.content[0].intakeId", is(12)))
                .andExpect(jsonPath("$.data.content[0].initialObservations").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].technicianId").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].statusId").doesNotExist());
    }

    @Test
    void historyIsCustomerScopedAndPaginated() throws Exception {
        allowCustomer("customer-token", 7L, 3L);
        LocalDateTime date = LocalDateTime.of(2026, 7, 20, 10, 30);
        when(historyQueryPort.findHistory(3L, 1, 10, null)).thenReturn(new CustomerPortalPage<>(
                List.of(new CustomerPortalHistoryEvent(
                        "JOB_STARTED", 30L, 8L, "Honda Accord 2022", date,
                        "Trabajo iniciado", "Trabajo en proceso", "Trabajo en proceso",
                        12L, 20L, 30L)),
                1, 10, 11, 2));

        mockMvc.perform(authenticatedGet(
                        "/api/v1/customer-portal/history?page=1&size=10",
                        "customer-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page", is(1)))
                .andExpect(jsonPath("$.data.content[0].eventType", is("JOB_STARTED")));
    }

    @Test
    void customerCanAccessEveryOwnedPhaseCResource() throws Exception {
        allowCustomer("customer-token", 7L, 3L);
        LocalDateTime date = LocalDateTime.of(2026, 7, 20, 10, 30);
        CustomerPortalVehicleInfo vehicle = new CustomerPortalVehicleInfo(
                8L, "Honda", "Accord", 2022, "ABC-123", "Honda Accord 2022");
        when(intakeQueryPort.findIntakes(3L, 0, 20, null))
                .thenReturn(new CustomerPortalPage<>(List.of(), 0, 20, 0, 0));
        when(intakeQueryPort.findIntake(3L, 12L)).thenReturn(Optional.of(
                new CustomerPortalIntakeDetail(
                        12L, vehicle, date, 45000, "Ruido", "En diagnÃ³stico", date,
                        List.of())));
        when(workOrderQueryPort.findWorkOrders(3L, 0, 20, null, null, false))
                .thenReturn(new CustomerPortalPage<>(List.of(), 0, 20, 0, 0));
        when(workOrderQueryPort.findWorkOrder(3L, 20L)).thenReturn(Optional.of(
                new CustomerPortalWorkOrderDetail(
                        20L, 12L, vehicle, date, "Ruido", date, "Servicio en proceso",
                        date, date.plusDays(1), new BigDecimal("2.5000"), true, 30L)));
        when(workOrderQueryPort.ownsWorkOrder(3L, 20L)).thenReturn(true);
        when(workOrderQueryPort.findVisibleQuotation(3L, 20L)).thenReturn(Optional.of(
                new CustomerPortalQuotation(
                        20L, 40L, 1, "CotizaciÃ³n autorizada", "MXN",
                        new BigDecimal("100.0000"), true, new BigDecimal("0.160000"),
                        new BigDecimal("16.0000"), new BigDecimal("116.0000"), date,
                        date.plusDays(1), new BigDecimal("2.5000"), "Notas", date,
                        "Taller", List.of(), List.of())));
        when(jobQueryPort.findJobs(3L, 0, 20, null, null))
                .thenReturn(new CustomerPortalPage<>(List.of(), 0, 20, 0, 0));
        when(jobQueryPort.findJob(3L, 30L)).thenReturn(Optional.of(
                new CustomerPortalJobDetail(
                        30L, 20L, vehicle, "Trabajo en proceso", date, date, null,
                        "TÃ©cnico QA", "MecÃ¡nica general", 20L, false,
                        List.of(), List.of())));
        when(historyQueryPort.findHistory(3L, 0, 20, null))
                .thenReturn(new CustomerPortalPage<>(List.of(), 0, 20, 0, 0));

        for (String path : List.of(
                "/api/v1/customer-portal/vehicle-intakes",
                "/api/v1/customer-portal/vehicle-intakes/12",
                "/api/v1/customer-portal/work-orders",
                "/api/v1/customer-portal/work-orders/20",
                "/api/v1/customer-portal/work-orders/20/quotation",
                "/api/v1/customer-portal/jobs",
                "/api/v1/customer-portal/jobs/30",
                "/api/v1/customer-portal/history")) {
            mockMvc.perform(authenticatedGet(path, "customer-token"))
                    .andExpect(status().isOk());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/v1/customer-portal/vehicle-intakes?page=-1",
            "/api/v1/customer-portal/work-orders?size=0",
            "/api/v1/customer-portal/jobs?size=101",
            "/api/v1/customer-portal/history?vehicleId=0"
    })
    void phaseCInvalidParametersReturnBadRequest(String path) throws Exception {
        allowCustomer("customer-token", 7L, 3L);
        mockMvc.perform(authenticatedGet(path, "customer-token"))
                .andExpect(status().isBadRequest());
    }

    private void allowCustomer(String token, Long userId, Long customerId) {
        tokenRepresents(token, userId, "CLIENTE");
        when(customerRepository.findByUserId(userId)).thenReturn(Optional.of(customer(customerId, userId)));
    }

    private void tokenRepresents(String token, Long userId, String role) {
        when(jwtService.parse(token)).thenReturn(new AuthenticatedUser(
                userId, "user@mechsync.local", Set.of(role)));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authenticatedGet(
            String path, String token) {
        return get(path).header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    private Customer customer(Long customerId, Long userId) {
        LocalDateTime timestamp = LocalDateTime.of(2026, 7, 21, 12, 0);
        return new Customer(customerId, userId, null, timestamp, timestamp, null);
    }

    private Vehicle vehicle(Long vehicleId, Long customerId) {
        return new Vehicle(
                vehicleId, customerId, "Honda", "Accord", 2022, "Rojo", "ABC-123",
                "1HGCM82633A001234", 45000, LocalDateTime.of(2026, 7, 21, 12, 0), null);
    }

    private List<String> allPortalPaths() {
        return List.of(
                PROFILE,
                VEHICLES,
                VEHICLES + "/8",
                "/api/v1/customer-portal/vehicle-intakes",
                "/api/v1/customer-portal/vehicle-intakes/12",
                "/api/v1/customer-portal/work-orders",
                "/api/v1/customer-portal/work-orders/20",
                "/api/v1/customer-portal/work-orders/20/quotation",
                "/api/v1/customer-portal/jobs",
                "/api/v1/customer-portal/jobs/30",
                "/api/v1/customer-portal/history");
    }
}
