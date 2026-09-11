package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import com.backend.nutri_predic.common.exception.BusinessException;
import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.estudio.repository.ParticipacionEstudioRepository;
import com.backend.nutri_predic.prediccionmodelo.evento.dto.InicioAnalisisRequest;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.EventoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.OrigenResultadoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.ProcedimientoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.TipoProcedimientoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.repository.EventoAnalisisRepository;
import com.backend.nutri_predic.prediccionmodelo.evento.repository.ProcedimientoAnalisisRepository;
import com.backend.nutri_predic.prediccionmodelo.evento.service.EventoAnalisisLifecycleService;
import com.backend.nutri_predic.prediccionmodelo.evento.service.EventoAnalisisService;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.service.ModeloPredictivoService;
import com.backend.nutri_predic.prediccionmodelo.service.PrediccionModeloFallidaException;
import com.backend.nutri_predic.prediccionmodelo.service.ResultadoInferenciaModelo;
import com.backend.nutri_predic.usuario.repository.UsuarioRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class EventoAnalisisServiceTests {
    @Mock EventoAnalisisRepository eventos;
    @Mock ProcedimientoAnalisisRepository procedimientos;
    @Mock ParticipacionEstudioRepository participaciones;
    @Mock UsuarioRepository usuarios;
    @Mock AccessService access;
    @Mock ModeloPredictivoService modelo;
    @Mock EventoAnalisisLifecycleService lifecycle;
    @Mock Authentication authentication;
    @Mock Cliente cliente;

    private EventoAnalisisService service;
    private ProcedimientoAnalisis procedimiento;

    @BeforeEach
    void setUp() {
        service =
                new EventoAnalisisService(
                        eventos,
                        procedimientos,
                        participaciones,
                        usuarios,
                        access,
                        modelo,
                        lifecycle);
        procedimiento = new ProcedimientoAnalisis();
        procedimiento.setCodigo("SOFTWARE_IA");
        procedimiento.setVersion(1);
        procedimiento.setNombre("Modelo predictivo");
        procedimiento.setTipo(TipoProcedimientoAnalisis.SOFTWARE_IA);
    }

    @Test
    void endpointCompatibleSoftwareIaUsaInstrumentacionYModeloOficial() {
        EventoAnalisis evento = prepararSolicitud();
        PrediccionModelo prediccion = org.mockito.Mockito.mock(PrediccionModelo.class);
        when(prediccion.getId()).thenReturn(44L);
        when(modelo.predecirConTrazabilidad(any(), any()))
                .thenReturn(
                        new ResultadoInferenciaModelo(
                                prediccion, OrigenResultadoAnalisis.GENERADO));
        when(lifecycle.cerrarExitoso(isNull(), eq(44L), eq(OrigenResultadoAnalisis.GENERADO)))
                .thenAnswer(
                        invocacion -> {
                            evento.prepararResultadoExitoso(
                                    prediccion, OrigenResultadoAnalisis.GENERADO);
                            evento.confirmarResultadoDisponible(Instant.now());
                            return evento;
                        });

        var respuesta = service.iniciar(request(), authentication);

        verify(lifecycle, times(1)).crear(any(), any(), any(), any(), isNull(), isNull());
        verify(lifecycle).cerrarExitoso(isNull(), eq(44L), eq(OrigenResultadoAnalisis.GENERADO));
        assertThat(respuesta.prediccionModeloId()).isEqualTo(44L);
        assertThat(respuesta.origenResultado()).isEqualTo("GENERADO");
        assertThat(respuesta.analisisIniciadoEn()).isNotNull();
        assertThat(respuesta.resultadoDisponibleEn())
                .isAfterOrEqualTo(respuesta.analisisIniciadoEn());
    }

    @Test
    void falloMlConservaEventoInvalidoYVinculoAlIntentoFallido() {
        EventoAnalisis evento = prepararSolicitud();
        when(modelo.predecirConTrazabilidad(any(), any()))
                .thenThrow(
                        new PrediccionModeloFallidaException(
                                "fallo", 44L, new RuntimeException("causa")));
        prepararInvalidacion(evento);

        var respuesta = service.iniciar(request(), authentication);

        assertThat(respuesta.estadoValidez()).isEqualTo("INVALIDA");
        assertThat(respuesta.resultadoDisponibleEn()).isNull();
        verify(lifecycle)
                .marcarInvalido(
                        isNull(), anyString(), org.mockito.ArgumentMatchers.eq(44L), isNull());
    }

    @Test
    void insuficienciaDeDatosNoFabricaModeloRespondioEn() {
        EventoAnalisis evento = prepararSolicitud();
        when(modelo.predecirConTrazabilidad(any(), any()))
                .thenThrow(new BusinessException("sin datos"));
        prepararInvalidacion(evento);

        var respuesta = service.iniciar(request(), authentication);

        assertThat(respuesta.estadoValidez()).isEqualTo("INVALIDA");
        assertThat(respuesta.modeloRespondioEn()).isNull();
        verify(lifecycle, never()).registrarModeloRespondio(anyLong(), any());
    }

    @Test
    void excepcionInesperadaConservaEventoInvalidoYSePropaga() {
        EventoAnalisis evento = prepararSolicitud();
        IllegalStateException original = new IllegalStateException("inesperado");
        when(modelo.predecirConTrazabilidad(any(), any())).thenThrow(original);
        prepararInvalidacion(evento);

        assertThatThrownBy(() -> service.iniciar(request(), authentication)).isSameAs(original);
        assertThat(evento.getEstadoValidez()).isEqualTo(EstadoValidezMedicion.INVALIDA);
        assertThat(evento.getResultadoDisponibleEn()).isNull();
    }

    private EventoAnalisis prepararSolicitud() {
        when(procedimientos.findById(2L)).thenReturn(Optional.of(procedimiento));
        when(access.client(1L, authentication)).thenReturn(cliente);
        when(cliente.getId()).thenReturn(1L);
        when(authentication.getName()).thenReturn("cliente@test.local");
        when(usuarios.findByEmail("cliente@test.local")).thenReturn(Optional.empty());
        EventoAnalisis evento = new EventoAnalisis();
        evento.setCliente(cliente);
        evento.setProcedimiento(procedimiento);
        evento.setFechaCorte(LocalDate.of(2026, 8, 30));
        evento.setMomento(MomentoEvaluacion.BASAL);
        evento.iniciarAnalisis(Instant.now().minusSeconds(1));
        when(lifecycle.crear(any(), any(), any(), any(), isNull(), isNull())).thenReturn(evento);
        return evento;
    }

    private void prepararInvalidacion(EventoAnalisis evento) {
        when(lifecycle.marcarInvalido(isNull(), anyString(), any(), any()))
                .thenAnswer(
                        invocacion -> {
                            evento.marcarInvalido(
                                    invocacion.getArgument(1), null, invocacion.getArgument(3));
                            return evento;
                        });
    }

    private InicioAnalisisRequest request() {
        return new InicioAnalisisRequest(
                1L, 2L, LocalDate.of(2026, 8, 30), null, MomentoEvaluacion.BASAL);
    }
}
