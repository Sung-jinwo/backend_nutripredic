package com.backend.nutri_predic.conocimiento.practica.controller;

import com.backend.nutri_predic.common.dto.ApiErrorResponse;
import com.backend.nutri_predic.conocimiento.practica.dto.*;
import com.backend.nutri_predic.conocimiento.practica.service.GeneracionPreguntasConocimientoService;
import com.backend.nutri_predic.conocimiento.practica.service.RespuestaConocimientoIaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes/{clienteId}/conocimiento/post-modelo")
@Tag(name = "PCC-IA complementario")
public class ConocimientoPostModeloController {
    private final GeneracionPreguntasConocimientoService service;
    private final RespuestaConocimientoIaService respuestas;

    public ConocimientoPostModeloController(
            GeneracionPreguntasConocimientoService s, RespuestaConocimientoIaService respuestas) {
        service = s;
        this.respuestas = respuestas;
    }

    @PostMapping("/generar")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Generar preguntas adaptativas complementarias",
            description =
                    "Usa la última predicción V5 exitosa. La respuesta pública no incluye soluciones.")
    @ApiResponse(
            responseCode = "201",
            description = "Sesión adaptativa generada o recuperada",
            content =
                    @Content(
                            schema =
                                    @Schema(
                                            implementation =
                                                    SesionConocimientoPublicaResponse.class)))
    public SesionConocimientoPublicaResponse generar(
            @PathVariable Long clienteId,
            @Valid @RequestBody GenerarConocimientoIaRequest request,
            Authentication auth) {
        return service.generar(clienteId, request, auth);
    }

    @PostMapping("/{sesionId}/respuestas")
    @Operation(
            summary = "Responder una sesión adaptativa",
            description =
                    "Acepta una única entrega con el conjunto exacto de preguntas. El resultado es complementario y no modifica ResultadoTest ni PCC.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Sesión respondida con feedback educativo",
                content =
                        @Content(
                                schema =
                                        @Schema(
                                                implementation =
                                                        SesionConocimientoResultadoResponse
                                                                .class))),
        @ApiResponse(
                responseCode = "400",
                description = "Entrega inválida, incompleta, duplicada o ya respondida",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Sesión inexistente o ajena al cliente",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public SesionConocimientoResultadoResponse responder(
            @PathVariable Long clienteId,
            @PathVariable Long sesionId,
            @Valid @RequestBody ResponderConocimientoIaRequest request,
            Authentication auth) {
        return respuestas.responder(clienteId, sesionId, request, auth);
    }

    @GetMapping
    @Operation(
            summary = "Obtener la última sesión PCC-IA",
            description =
                    "En estado GENERADA oculta soluciones; en RESPONDIDA incluye feedback educativo complementario.")
    @ApiResponse(
            responseCode = "200",
            description = "Sesión adaptativa pública",
            content =
                    @Content(
                            schema =
                                    @Schema(
                                            implementation =
                                                    SesionConocimientoPublicaResponse.class)))
    public SesionConocimientoPublicaResponse obtener(
            @PathVariable Long clienteId,
            @RequestParam(required = false) java.time.LocalDate fecha,
            Authentication auth) {
        return service.obtener(clienteId, fecha, auth);
    }
}
