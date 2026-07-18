package com.mechsync.modules.servicereports.infrastructure.repository;

import java.math.BigDecimal;

public interface JobClosureView {
    Long getJobId();
    String getStatusCode();
    BigDecimal getActualSubtotal();
    BigDecimal getActualIva();
    BigDecimal getActualTotal();
}
