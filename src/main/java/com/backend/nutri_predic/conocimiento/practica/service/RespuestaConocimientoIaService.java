package com.backend.nutri_predic.conocimiento.practica.service;

import com.backend.nutri_predic.common.exception.BusinessException;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.conocimiento.practica.dto.ResponderConocimientoIaRequest;
import com.backend.nutri_predic.conocimiento.practica.dto.SesionConocimientoResultadoResponse;
import com.backend.nutri_predic.conocimiento.practica.entity.EstadoSesionConocimientoIa;
import com.backend.nutri_predic.conocimiento.practica.entity.PreguntaGeneradaIa;
import com.backend.nutri_predic.conocimiento.practica.entity.RespuestaAdaptativaIa;
import com.backend.nutri_predic.conocimiento.practica.repository.PreguntaGeneradaIaRepository;
import com.backend.nutri_predic.conocimiento.practica.repository.RespuestaAdaptativaIaRepository;
import com.backend.nutri_predic.conocimiento.practica.repository.SesionConocimientoIaRepository;
import com.backend.nutri_predic.prediccionmodelo.entity.EstadoPrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.service.ModeloPredictivoV6Service;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RespuestaConocimientoIaService {
    private final SesionConocimientoIaRepository sesiones;
    private final PreguntaGeneradaIaRepository preguntas;
    private final RespuestaAdaptativaIaRepository respuestas;
    private final AccessService access;

    public RespuestaConocimientoIaService(
            SesionConocimientoIaRepository sesiones,
            PreguntaGeneradaIaRepository preguntas,
            RespuestaAdaptativaIaRepository respuestas,
            AccessService access) {
        this.sesiones = sesiones;
        this.preguntas = preguntas;
        this.respuestas = respuestas;
        this.access = access;
    }

    @Transactional
    public SesionConocimientoResultadoResponse responder(
            Long clienteId,
            Long sesionId,
            ResponderConocimientoIaRequest request,
            Authentication authentication) {
        access.client(clienteId, authentication);
        var sesion =
                sesiones.findByIdForUpdate(sesionId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Sesión de conocimiento adaptativo"));
        Long propietario = sesion.getClienteId() != null ? sesion.getClienteId() : sesion.getPrediccionModelo().getCliente().getId();
        if (!propietario.equals(clienteId)) {
            throw new ResourceNotFoundException("Sesión de conocimiento adaptativo");
        }
        if (sesion.getEstado() == EstadoSesionConocimientoIa.RESPONDIDA
                || respuestas.existsBySesionId(sesionId)) {
            throw new BusinessException("La sesión adaptativa ya fue respondida");
        }
        if (sesion.getEstado() != EstadoSesionConocimientoIa.GENERADA) {
            throw new BusinessException("La sesión adaptativa no admite respuestas");
        }

        List<PreguntaGeneradaIa> preguntasSesion =
                preguntas.findBySesionIdOrderByOrdenAsc(sesionId);
        validarConjuntoExacto(request, preguntasSesion);

        Map<Long, ResponderConocimientoIaRequest.Respuesta> entregadas = new HashMap<>();
        request.respuestas()
                .forEach(respuesta -> entregadas.put(respuesta.preguntaId(), respuesta));
        Instant respondidaEn = Instant.now();
        List<RespuestaAdaptativaIa> nuevas = new ArrayList<>();
        for (PreguntaGeneradaIa pregunta : preguntasSesion) {
            String opcion =
                    entregadas
                            .get(pregunta.getId())
                            .opcionSeleccionada()
                            .trim()
                            .toUpperCase(Locale.ROOT);
            validarOpcionExistente(pregunta, opcion);
            nuevas.add(
                    new RespuestaAdaptativaIa(
                            sesion,
                            pregunta,
                            opcion,
                            pregunta.getRespuestaCorrecta().equalsIgnoreCase(opcion),
                            respondidaEn));
        }

        List<RespuestaAdaptativaIa> persistidas = respuestas.saveAll(nuevas);
        int correctas = (int) persistidas.stream().filter(RespuestaAdaptativaIa::isCorrecta).count();
        java.math.BigDecimal puntaje = java.math.BigDecimal.valueOf(correctas * 2L);
        sesion.setPuntajeObtenido(puntaje);
        sesion.setPuntajeMaximo(java.math.BigDecimal.TEN);
        sesion.setNivelResultado(
                puntaje.compareTo(java.math.BigDecimal.TEN) == 0
                        ? "ALTO"
                        : puntaje.compareTo(java.math.BigDecimal.valueOf(7)) >= 0
                                ? "INTERMEDIO"
                                : "BAJO");
        sesion.marcarRespondida(respondidaEn);
        validarComoMedicionOficial(sesion, preguntasSesion.size(), persistidas.size());
        sesiones.save(sesion);
        return SesionConocimientoResultadoResponse.from(sesion, preguntasSesion, persistidas);
    }

    private void validarComoMedicionOficial(
            com.backend.nutri_predic.conocimiento.practica.entity.SesionConocimientoIa sesion,
            int totalPreguntas,
            int totalRespuestas) {
        var prediccion = sesion.getPrediccionModelo();
        boolean valida = prediccion != null
                && prediccion.getEstado() == EstadoPrediccionModelo.EXITOSA
                && prediccion.getMomentoEvaluacion() == MomentoEvaluacion.DIARIO
                && ModeloPredictivoV6Service.esModeloDiarioAdmitido(prediccion.getModelVersion())
                && "pcc-ia-v1".equals(sesion.getConfiguracionVersion())
                && totalPreguntas == 5
                && totalRespuestas == 5
                && java.math.BigDecimal.TEN.compareTo(sesion.getPuntajeMaximo()) == 0
                && sesion.getPuntajeObtenido() != null
                && java.util.Set.of("BAJO", "INTERMEDIO", "ALTO").contains(sesion.getNivelResultado());
        sesion.setEstadoValidez(valida ? EstadoValidezMedicion.VALIDA : EstadoValidezMedicion.INVALIDA);
        sesion.setMotivoInvalidez(valida ? null : "SESION_NO_CUMPLE_CONTRATO_PCC_DIARIO");
    }

    private void validarConjuntoExacto(
            ResponderConocimientoIaRequest request, List<PreguntaGeneradaIa> preguntasSesion) {
        if (preguntasSesion.isEmpty()) {
            throw new BusinessException("La sesión adaptativa no contiene preguntas");
        }
        Set<Long> idsEntregados = new HashSet<>();
        for (var respuesta : request.respuestas()) {
            if (!idsEntregados.add(respuesta.preguntaId())) {
                throw new BusinessException("La entrega contiene preguntas duplicadas");
            }
        }
        Set<Long> idsSesion = new HashSet<>();
        preguntasSesion.forEach(pregunta -> idsSesion.add(pregunta.getId()));
        if (!idsSesion.containsAll(idsEntregados)) {
            throw new BusinessException("La entrega contiene preguntas ajenas a la sesión");
        }
        if (!idsEntregados.equals(idsSesion)) {
            throw new BusinessException(
                    "La entrega debe contener exactamente todas las preguntas de la sesión");
        }
    }

    private void validarOpcionExistente(PreguntaGeneradaIa pregunta, String opcion) {
        String texto =
                switch (opcion) {
                    case "A" -> pregunta.getOpcionA();
                    case "B" -> pregunta.getOpcionB();
                    case "C" -> pregunta.getOpcionC();
                    case "D" -> pregunta.getOpcionD();
                    default -> null;
                };
        if (texto == null || texto.isBlank()) {
            throw new BusinessException("La opción seleccionada no existe");
        }
    }
}
