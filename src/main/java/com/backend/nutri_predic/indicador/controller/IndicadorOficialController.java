package com.backend.nutri_predic.indicador.controller;

import com.backend.nutri_predic.indicador.dto.PccIndicatorResponse;
import com.backend.nutri_predic.indicador.dto.PcsIndicatorResponse;
import com.backend.nutri_predic.indicador.dto.TppIndicatorResponse;
import com.backend.nutri_predic.indicador.service.PccIndicatorService;
import com.backend.nutri_predic.indicador.service.PcsIndicatorService;
import com.backend.nutri_predic.indicador.service.TppIndicatorService;
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
@RequestMapping("/api/admin/indicadores")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Indicadores oficiales")
public class IndicadorOficialController {
    private final PccIndicatorService pcc;
    private final PcsIndicatorService pcs;
    private final TppIndicatorService tpp;

    public IndicadorOficialController(
            PccIndicatorService pcc, PcsIndicatorService pcs, TppIndicatorService tpp) {
        this.pcc = pcc;
        this.pcs = pcs;
        this.tpp = tpp;
    }

    @GetMapping("/pcc")
    @Operation(summary = "Obtener PCC oficial")
    @ApiResponse(
            responseCode = "200",
            description = "PCC disponible o no disponible",
            content = @Content(schema = @Schema(implementation = PccIndicatorResponse.class)))
    public PccIndicatorResponse pcc() {
        return pcc.obtener();
    }

    @GetMapping("/pcs")
    @Operation(summary = "Obtener PCS oficial")
    @ApiResponse(
            responseCode = "200",
            description = "PCS disponible o no disponible",
            content = @Content(schema = @Schema(implementation = PcsIndicatorResponse.class)))
    public PcsIndicatorResponse pcs() {
        return pcs.obtener();
    }

    @GetMapping("/tpp")
    @Operation(
            summary = "Obtener TPP oficial",
            description = "No utiliza inferenceMs; mide EventoAnalisis SOFTWARE_IA completo.")
    @ApiResponse(
            responseCode = "200",
            description = "TPP disponible o no disponible",
            content = @Content(schema = @Schema(implementation = TppIndicatorResponse.class)))
    public TppIndicatorResponse tpp() {
        return tpp.obtener();
    }
}
