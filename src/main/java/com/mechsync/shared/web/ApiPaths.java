package com.mechsync.shared.web;

public final class ApiPaths {

    public static final String API_V1 = "/api/v1";
    public static final String HEALTH = API_V1 + "/health";
    public static final String HEALTH_DATABASE = HEALTH + "/database";
    public static final String AUTH = API_V1 + "/auth";
    public static final String AUTH_LOGIN = AUTH + "/login";
    public static final String AUTH_ME = AUTH + "/me";
    public static final String CUSTOMERS = API_V1 + "/customers";
    public static final String USERS = API_V1 + "/users";
    public static final String VEHICLES = API_V1 + "/vehicles";
    public static final String VEHICLE_INTAKES = API_V1 + "/vehicle-intakes";
    public static final String WORK_ORDERS = API_V1 + "/work-orders";
    public static final String WORK_ORDER_REVISIONS = WORK_ORDERS + "/{workOrderId}/revisions";
    public static final String CATALOGS = API_V1 + "/catalogs";
    public static final String CATALOG_STATUSES = CATALOGS + "/statuses";
    public static final String TECHNICIANS = API_V1 + "/technicians";
    public static final String SPECIALTIES = API_V1 + "/specialties";
    public static final String JOBS = API_V1 + "/jobs";
    public static final String SERVICES = API_V1 + "/services";
    public static final String PARTS = API_V1 + "/parts";
    public static final String SERVICE_REPORTS = API_V1 + "/service-reports";
    public static final String DASHBOARD = API_V1 + "/dashboard";
    public static final String CUSTOMER_PORTAL = API_V1 + "/customer-portal";

    private ApiPaths() {
    }
}
