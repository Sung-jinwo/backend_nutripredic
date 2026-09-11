package com.backend.nutri_predic.prediccionmodelo.controller;

import com.backend.nutri_predic.common.dto.ApiErrorResponse;
import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.prediccionmodelo.evento.service.EventoAnalisisService;
import com.backend.nutri_predic.prediccionmodelo.dto.*;
import com.backend.nutri_predic.prediccionmodelo.service.ModeloPredictivoV6Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Análisis predictivo V6")
public class AnalisisPredictivoController {
    private final ModeloPredictivoV6Service service;
    private final AccessService access;
    private final EventoAnalisisService eventosAnalisis;

    public AnalisisPredictivoController(
            ModeloPredictivoV6Service service,
            AccessService access,
            EventoAnalisisService eventosAnalisis) {
        this.service = service;
        this.access = access;
        this.eventosAnalisis = eventosAnalisis;
    }

    @PostMapping("/api/analisis-predictivo")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Ejecutar el análisis predictivo oficial",
            description =
                    "Crea un EventoAnalisis SOFTWARE_IA y genera o reutiliza una predicción V6 técnica. inferenceMs es metadata técnica y no es TPP.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Resultado generado o reutilizado",
                content =
                        @Content(
                                schema =
                                        @Schema(
                                                implementation =
                                                        AnalisisPredictivoResponse.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Solicitud inválida o datos técnicamente insuficientes",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Cliente o procedimiento SOFTWARE_IA no encontrado",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "No autenticado",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Sin acceso al cliente",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "502",
                description = "Modelo no disponible o error predictivo",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public AnalisisPredictivoResponse predecir(
            @Valid @RequestBody AnalisisPredictivoRequest request, Authentication authentication) {
        return eventosAnalisis.predecirInstrumentadoV6(request, authentication);
    }

    @GetMapping("/api/clientes/{clienteId}/analisis-predictivo/preparacion")
    public PreparacionAnalisisPredictivoV6Response preparacion(
            @PathVariable Long clienteId, @RequestParam java.time.LocalDate fechaCorte, Authentication authentication) {
        access.client(clienteId, authentication);
        return service.preparacion(clienteId, fechaCorte);
    }

    @GetMapping("/api/clientes/{clienteId}/predicciones-modelo")
    public List<PrediccionModeloHistorialResponse> historial(
            @PathVariable Long clienteId, Authentication authentication) {
        access.client(clienteId, authentication);
        return service.historial(clienteId);
    }
}
