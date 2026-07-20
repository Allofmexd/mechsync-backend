package com.mechsync.modules.servicereports.infrastructure.repository;

import java.math.BigDecimal;

public interface ServiceReportPdfPartLineView {
    String getName();
    BigDecimal getQuantity();
    String getMeasurementUnit();
    BigDecimal getUnitPrice();
    BigDecimal getSubtotal();
}
