package com.mechsync.modules.servicereports.infrastructure.repository;

import java.math.BigDecimal;

public interface ServiceReportPdfServiceLineView {
    String getName();
    BigDecimal getQuantity();
    BigDecimal getUnitPrice();
    BigDecimal getSubtotal();
}
