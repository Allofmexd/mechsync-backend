package com.mechsync.modules.jobs.infrastructure.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface JobPartLineView {
    Long getLineId();
    Long getJobId();
    Long getCatalogId();
    String getCatalogName();
    BigDecimal getQuantity();
    BigDecimal getUnitPrice();
    BigDecimal getLineSubtotal();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
