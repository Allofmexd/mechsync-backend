package com.mechsync.modules.dashboard.domain.model;

import java.math.BigDecimal;

public record TopServiceMetric(Long serviceId, String serviceName, BigDecimal quantity) {
}
