package com.backend.nutri_predic.prediccionmodelo.evento.service;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.estudio.entity.ParticipacionEstudio;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.EventoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.OrigenResultadoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.ProcedimientoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.repository.EventoAnalisisRepository;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import com.backend.nutri_predic.usuario.entity.Usuario;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventoAnalisisLifecycleService {
    private final EventoAnalisisRepository eventos;
    private final PrediccionModeloRepository predicciones;

    public EventoAnalisisLifecycleService(
            EventoAnalisisRepository eventos, PrediccionModeloRepository predicciones) {
        this.eventos = eventos;
        this.predicciones = predicciones;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventoAnalisis crear(
            Cliente cliente,
            ProcedimientoAnalisis procedimiento,
            LocalDate fechaCorte,
            MomentoEvaluacion momento,
            ParticipacionEstudio participacion,
            Usuario evaluador) {
        EventoAnalisis evento = new EventoAnalisis();
        evento.setCliente(cliente);
        evento.setProcedimiento(procedimiento);
        evento.setFechaCorte(fechaCorte);
        evento.setMomento(momento);
        evento.setParticipacionEstudio(participacion);
        evento.setEvaluador(evaluador);
        evento.iniciarAnalisis(Instant.now());
        return eventos.saveAndFlush(evento);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarVariablesPreparadas(Long eventoId, Instant instante) {
        EventoAnalisis evento = obtener(eventoId);
        evento.registrarVariablesPreparadas(instante);
        eventos.saveAndFlush(evento);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarModeloSolicitado(Long eventoId, Instant instante) {
        EventoAnalisis evento = obtener(eventoId);
        evento.registrarModeloSolicitado(instante);
        eventos.saveAndFlush(evento);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarModeloRespondio(Long eventoId, Instant instante) {
        EventoAnalisis evento = obtener(eventoId);
        evento.registrarModeloRespondio(instante);
        eventos.saveAndFlush(evento);
    }

    /**
     * El TPP termina cuando el resultado ya tiene vínculo, origen y validez
     * enviados satisfactoriamente a la base de datos y puede cerrarse para ser
     * retornado por el backend. No incluye renderizado del frontend.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventoAnalisis cerrarExitoso(
            Long eventoId, Long prediccionId, OrigenResultadoAnalisis origen) {
        EventoAnalisis evento = obtener(eventoId);
        PrediccionModelo prediccion =
                predicciones
                        .findById(prediccionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Predicción de modelo"));

        evento.prepararResultadoExitoso(prediccion, origen);
        eventos.saveAndFlush(evento);

        evento.confirmarResultadoDisponible(Instant.now());
        return eventos.saveAndFlush(evento);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventoAnalisis marcarInvalido(
            Long eventoId, String motivo, Long prediccionId, OrigenResultadoAnalisis origen) {
        EventoAnalisis evento = obtener(eventoId);
        PrediccionModelo prediccion =
                prediccionId == null
                        ? null
                        : predicciones
                                .findById(prediccionId)
                                .orElseThrow(
                                        () ->
                                                new ResourceNotFoundException(
                                                        "Predicción de modelo"));
        evento.marcarInvalido(motivo, prediccion, origen);
        return eventos.saveAndFlush(evento);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventoAnalisis cerrarManual(Long eventoId, String observaciones) {
        EventoAnalisis evento = obtener(eventoId);
        evento.cerrarManual(Instant.now(), observaciones);
        return eventos.saveAndFlush(evento);
    }

    private EventoAnalisis obtener(Long eventoId) {
        return eventos.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento de análisis"));
    }
}
