package com.mechsync.modules.vehicles.web.controller;

import com.mechsync.modules.vehicles.application.dto.VehiclePage;
import java.util.List;

public record VehiclePageResponse(
        List<VehicleResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
    public VehiclePageResponse {
        content = List.copyOf(content);
    }

    public static VehiclePageResponse from(VehiclePage result) {
        return new VehiclePageResponse(
                result.content().stream().map(VehicleResponse::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
    }
}
