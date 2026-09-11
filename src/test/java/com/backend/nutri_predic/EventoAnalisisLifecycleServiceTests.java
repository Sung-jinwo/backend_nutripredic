package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.EventoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.OrigenResultadoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.ProcedimientoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.TipoProcedimientoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.repository.EventoAnalisisRepository;
import com.backend.nutri_predic.prediccionmodelo.evento.service.EventoAnalisisLifecycleService;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventoAnalisisLifecycleServiceTests {
    @Mock EventoAnalisisRepository eventos;
    @Mock PrediccionModeloRepository predicciones;

    private EventoAnalisisLifecycleService lifecycle;

    @BeforeEach
    void setUp() {
        lifecycle = new EventoAnalisisLifecycleService(eventos, predicciones);
    }

    @Test
    void resultadoGeneradoVinculaPrediccionYSeCierraTrasFlushPrevio() {
        EventoAnalisis evento = eventoSoftwareIa();
        PrediccionModelo prediccion = new PrediccionModelo();
        when(eventos.findById(1L)).thenReturn(Optional.of(evento));
        when(predicciones.findById(9L)).thenReturn(Optional.of(prediccion));
        AtomicInteger flush = new AtomicInteger();
        when(eventos.saveAndFlush(any()))
                .thenAnswer(
                        invocacion -> {
                            EventoAnalisis guardado = invocacion.getArgument(0);
                            int numero = flush.incrementAndGet();
                            if (numero == 1) {
                                assertThat(guardado.getPrediccionModelo()).isSameAs(prediccion);
                                assertThat(guardado.getOrigenResultado())
                                        .isEqualTo(OrigenResultadoAnalisis.GENERADO);
                                assertThat(guardado.getEstadoValidez())
                                        .isEqualTo(EstadoValidezMedicion.VALIDA);
                                assertThat(guardado.getResultadoDisponibleEn()).isNull();
                            }
                            return guardado;
                        });

        EventoAnalisis cerrado = lifecycle.cerrarExitoso(1L, 9L, OrigenResultadoAnalisis.GENERADO);

        assertThat(flush).hasValue(2);
        assertThat(cerrado.getResultadoDisponibleEn()).isNotNull();
        assertThat(cerrado.getResultadoDisponibleEn())
                .isAfterOrEqualTo(cerrado.getAnalisisIniciadoEn());
    }

    @Test
    void eventoReutilizadoEsNuevoYPuedeApuntarALaPrediccionGenerada() {
        EventoAnalisis primero = eventoSoftwareIa();
        EventoAnalisis segundo = eventoSoftwareIa();
        PrediccionModelo compartida = new PrediccionModelo();
        when(eventos.findById(1L)).thenReturn(Optional.of(primero));
        when(eventos.findById(2L)).thenReturn(Optional.of(segundo));
        when(predicciones.findById(9L)).thenReturn(Optional.of(compartida));
        when(eventos.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        lifecycle.cerrarExitoso(1L, 9L, OrigenResultadoAnalisis.GENERADO);
        lifecycle.cerrarExitoso(2L, 9L, OrigenResultadoAnalisis.REUTILIZADO);

        assertThat(primero.getPrediccionModelo()).isSameAs(compartida);
        assertThat(segundo.getPrediccionModelo()).isSameAs(compartida);
        assertThat(primero.getOrigenResultado()).isEqualTo(OrigenResultadoAnalisis.GENERADO);
        assertThat(segundo.getOrigenResultado()).isEqualTo(OrigenResultadoAnalisis.REUTILIZADO);
        assertThat(primero.getResultadoDisponibleEn()).isNotNull();
        assertThat(segundo.getResultadoDisponibleEn()).isNotNull();
        assertThat(primero.getAnalisisIniciadoEn()).isNotNull();
        assertThat(segundo.getAnalisisIniciadoEn()).isNotNull();
    }

    @Test
    void dobleCierreSoftwareIaEsRechazado() {
        EventoAnalisis evento = eventoSoftwareIa();
        PrediccionModelo prediccion = new PrediccionModelo();
        when(eventos.findById(1L)).thenReturn(Optional.of(evento));
        when(predicciones.findById(9L)).thenReturn(Optional.of(prediccion));
        when(eventos.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        lifecycle.cerrarExitoso(1L, 9L, OrigenResultadoAnalisis.GENERADO);

        assertThatThrownBy(() -> lifecycle.cerrarExitoso(1L, 9L, OrigenResultadoAnalisis.GENERADO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya fue cerrado");
    }

    @Test
    void intentoFallidoQuedaVinculadoSinResultadoDisponible() {
        EventoAnalisis evento = eventoSoftwareIa();
        PrediccionModelo fallida = new PrediccionModelo();
        when(eventos.findById(1L)).thenReturn(Optional.of(evento));
        when(predicciones.findById(9L)).thenReturn(Optional.of(fallida));
        when(eventos.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        EventoAnalisis resultado =
                lifecycle.marcarInvalido(1L, "Error técnico al consultar el modelo", 9L, null);

        assertThat(resultado.getPrediccionModelo()).isSameAs(fallida);
        assertThat(resultado.getEstadoValidez()).isEqualTo(EstadoValidezMedicion.INVALIDA);
        assertThat(resultado.getResultadoDisponibleEn()).isNull();
        assertThat(resultado.getOrigenResultado()).isNull();
    }

    @Test
    void eventoInvalidoNoPuedeReabrirseComoExitoso() {
        EventoAnalisis evento = eventoSoftwareIa();
        PrediccionModelo fallida = new PrediccionModelo();
        PrediccionModelo posterior = new PrediccionModelo();
        evento.marcarInvalido("Fallo de la ejecución", fallida, null);

        assertThatThrownBy(
                        () ->
                                evento.prepararResultadoExitoso(
                                        posterior, OrigenResultadoAnalisis.GENERADO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya fue cerrado");
        assertThat(evento.getPrediccionModelo()).isSameAs(fallida);
        assertThat(evento.getEstadoValidez()).isEqualTo(EstadoValidezMedicion.INVALIDA);
        assertThat(evento.getResultadoDisponibleEn()).isNull();
    }

    @Test
    void inicioNoPuedeSobrescribirse() {
        EventoAnalisis evento = eventoSoftwareIa();
        Instant original = evento.getAnalisisIniciadoEn();

        assertThatThrownBy(() -> evento.iniciarAnalisis(Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sólo puede asignarse una vez");
        assertThat(evento.getAnalisisIniciadoEn()).isEqualTo(original);
    }

    private EventoAnalisis eventoSoftwareIa() {
        ProcedimientoAnalisis procedimiento = new ProcedimientoAnalisis();
        procedimiento.setTipo(TipoProcedimientoAnalisis.SOFTWARE_IA);
        EventoAnalisis evento = new EventoAnalisis();
        evento.setProcedimiento(procedimiento);
        evento.iniciarAnalisis(Instant.now().minusSeconds(1));
        return evento;
    }
}
