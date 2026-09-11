package com.backend.nutri_predic.dashboard.controller;

import com.backend.nutri_predic.dashboard.dto.DashboardResponse;
import com.backend.nutri_predic.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Dashboard administrativo")
public class DashboardController {
    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Obtener dashboard oficial",
            description =
                    "Entrega PCC, PCS y TPP calculados por sus servicios oficiales; el frontend no debe recalcularlos.")
    @ApiResponse(
            responseCode = "200",
            description = "Dashboard disponible",
            content = @Content(schema = @Schema(implementation = DashboardResponse.class)))
    public DashboardResponse get() {
        return service.dashboard();
    }
}
