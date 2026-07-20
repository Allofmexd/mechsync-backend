package com.mechsync.modules.servicereports.infrastructure.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ServiceReportPdfHeaderView {
    Long getReportId();
    Long getJobId();
    String getReportStatus();
    LocalDateTime getReportDate();
    String getFinalDescription();
    BigDecimal getFinalSubtotal();
    BigDecimal getFinalIva();
    BigDecimal getFinalTotal();
    Boolean getCustomerConfirmation();
    LocalDateTime getDeliveredAt();
    Long getWorkOrderId();
    Long getVehicleIntakeId();
    Long getTechnicianId();
    String getTechnicianName();
    Long getCustomerId();
    String getCustomerName();
    Long getVehicleId();
    String getVehicleBrand();
    String getVehicleModel();
    Integer getVehicleYear();
    String getLicensePlate();
    String getVin();
    Integer getMileage();
}
