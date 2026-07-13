package com.mechsync.modules.workorders.web.controller;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record UpdateWorkOrderRequest(@NotNull @Positive Long technicianId,LocalDateTime workOrderDate,
 LocalDateTime estimatedStartDate,LocalDateTime estimatedDeliveryDate,
 @PositiveOrZero @Digits(integer=3,fraction=2) BigDecimal estimatedHours,
 @NotNull @PositiveOrZero @Digits(integer=8,fraction=2) BigDecimal estimatedSubtotal,
 @NotNull @PositiveOrZero @Digits(integer=8,fraction=2) BigDecimal estimatedIva,
 @NotNull @PositiveOrZero @Digits(integer=8,fraction=2) BigDecimal estimatedTotal,
 @Pattern(regexp=".*\\S.*",message="must not be blank") String technicalObservations,
 @NotNull @Positive Long statusId) { }
