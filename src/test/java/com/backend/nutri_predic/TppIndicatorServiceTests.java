package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.indicador.dto.EstadoDisponibilidadTpp;
import com.backend.nutri_predic.indicador.dto.MotivoExclusionTpp;
import com.backend.nutri_predic.indicador.service.TppIndicatorService;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.EventoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.ProcedimientoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.TipoProcedimientoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.repository.EventoAnalisisRepository;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TppIndicatorServiceTests {
    private static final Instant INICIO = Instant.parse("2026-08-30T12:00:00Z");

    @Mock EventoAnalisisRepository eventos;

    private TppIndicatorService tpp;

    @BeforeEach
    void setUp() {
        tpp = new TppIndicatorService(eventos);
    }

    @Test
    void softwareIaValidoEntraAlTppConTiempoIndividualCorrecto() {
        dadoEventos(eventoSoftwareIaValido(1_250));

        var resultado = tpp.obtener();

        assertThat(resultado.promedioTppMs()).isEqualTo(1_250.0);
        assertThat(resultado.totalAnalisisValidos()).isEqualTo(1);
        assertThat(resultado.totalAnalisisExcluidos()).isZero();
        assertThat(resultado.estadoDisponibilidad()).isEqualTo(EstadoDisponibilidadTpp.DISPONIBLE);
    }

    @Test
    void promediaVariosEventosValidos() {
        dadoEventos(
                eventoSoftwareIaValido(1_000),
                eventoSoftwareIaValido(2_000),
                eventoSoftwareIaValido(3_000));

        var resultado = tpp.obtener();

        assertThat(resultado.promedioTppMs()).isEqualTo(2_000.0);
        assertThat(resultado.totalAnalisisValidos()).isEqualTo(3);
    }

    @Test
    void excluyeInicioAusente() {
        EventoAnalisis evento =
                evento(
                        TipoProcedimientoAnalisis.SOFTWARE_IA,
                        null,
                        INICIO.plusMillis(1_000),
                        EstadoValidezMedicion.VALIDA);

        assertExclusion(evento, MotivoExclusionTpp.INICIO_AUSENTE);
    }

    @Test
    void excluyeResultadoAusente() {
        EventoAnalisis evento =
                evento(
                        TipoProcedimientoAnalisis.SOFTWARE_IA,
                        INICIO,
                        null,
                        EstadoValidezMedicion.VALIDA);

        assertExclusion(evento, MotivoExclusionTpp.RESULTADO_AUSENTE);
    }

    @Test
    void excluyeResultadoAnteriorAlInicio() {
        EventoAnalisis evento =
                evento(
                        TipoProcedimientoAnalisis.SOFTWARE_IA,
                        INICIO,
                        INICIO.minusMillis(1),
                        EstadoValidezMedicion.VALIDA);

        assertExclusion(evento, MotivoExclusionTpp.ORDEN_TEMPORAL_INVALIDO);
    }

    @Test
    void excluyeEventoInvalido() {
        EventoAnalisis evento =
                evento(
                        TipoProcedimientoAnalisis.SOFTWARE_IA,
                        INICIO,
                        INICIO.plusMillis(1_000),
                        EstadoValidezMedicion.INVALIDA);

        assertExclusion(evento, MotivoExclusionTpp.EVENTO_INVALIDO);
    }

    @Test
    void manualValidoNoEntraAlTpp() {
        EventoAnalisis manual = eventoManualValido(8_000);
        dadoEventos(manual);

        var resultado = tpp.obtener();

        assertThat(resultado.promedioTppMs()).isNull();
        assertThat(resultado.totalAnalisisValidos()).isZero();
        assertThat(resultado.totalAnalisisExcluidos()).isEqualTo(1);
        assertThat(resultado.exclusiones())
                .containsEntry(MotivoExclusionTpp.PROCEDIMIENTO_NO_APLICABLE, 1L);
    }

    @Test
    void combinacionManualYSoftwareIaUsaSoloSoftwareIa() {
        dadoEventos(eventoManualValido(50_000), eventoSoftwareIaValido(2_500));

        var resultado = tpp.obtener();

        assertThat(resultado.promedioTppMs()).isEqualTo(2_500.0);
        assertThat(resultado.totalAnalisisValidos()).isEqualTo(1);
        assertThat(resultado.totalAnalisisExcluidos()).isEqualTo(1);
        assertThat(resultado.exclusiones())
                .containsEntry(MotivoExclusionTpp.PROCEDIMIENTO_NO_APLICABLE, 1L);
    }

    @Test
    void sinSoftwareIaValidoDevuelveTppNull() {
        dadoEventos(eventoManualValido(1_000));

        var resultado = tpp.obtener();

        assertThat(resultado.promedioTppMs()).isNull();
        assertThat(resultado.totalAnalisisValidos()).isZero();
        assertThat(resultado.estadoDisponibilidad())
                .isEqualTo(EstadoDisponibilidadTpp.NO_DISPONIBLE);
        assertThat(resultado.motivoNoDisponible())
                .isEqualTo(TppIndicatorService.SIN_ANALISIS_VALIDOS);
    }

    @Test
    void inferenceMsNoAlteraTpp() {
        EventoAnalisis evento = eventoSoftwareIaValido(2_500);
        PrediccionModelo prediccion = new PrediccionModelo();
        prediccion.setTiempoInferenciaMs(999_999L);
        lenient().when(evento.getPrediccionModelo()).thenReturn(prediccion);
        dadoEventos(evento);

        var resultado = tpp.obtener();

        assertThat(resultado.promedioTppMs()).isEqualTo(2_500.0);
    }

    private void assertExclusion(EventoAnalisis evento, MotivoExclusionTpp motivo) {
        dadoEventos(evento);

        var resultado = tpp.obtener();

        assertThat(resultado.promedioTppMs()).isNull();
        assertThat(resultado.totalAnalisisValidos()).isZero();
        assertThat(resultado.totalAnalisisExcluidos()).isEqualTo(1);
        assertThat(resultado.exclusiones()).containsEntry(motivo, 1L);
    }

    private EventoAnalisis eventoSoftwareIaValido(long duracionMs) {
        return eventoValido(duracionMs, TipoProcedimientoAnalisis.SOFTWARE_IA);
    }

    private EventoAnalisis eventoManualValido(long duracionMs) {
        return eventoValido(duracionMs, TipoProcedimientoAnalisis.MANUAL);
    }

    private EventoAnalisis eventoValido(
            long duracionMs, TipoProcedimientoAnalisis tipoProcedimiento) {
        return evento(
                tipoProcedimiento,
                INICIO,
                INICIO.plusMillis(duracionMs),
                EstadoValidezMedicion.VALIDA);
    }

    private EventoAnalisis evento(
            TipoProcedimientoAnalisis tipoProcedimiento,
            Instant inicio,
            Instant resultado,
            EstadoValidezMedicion validez) {
        EventoAnalisis evento = mock(EventoAnalisis.class);
        ProcedimientoAnalisis procedimiento = new ProcedimientoAnalisis();
        procedimiento.setTipo(tipoProcedimiento);
        lenient().when(evento.getProcedimiento()).thenReturn(procedimiento);
        lenient().when(evento.getAnalisisIniciadoEn()).thenReturn(inicio);
        lenient().when(evento.getResultadoDisponibleEn()).thenReturn(resultado);
        lenient().when(evento.getEstadoValidez()).thenReturn(validez);
        return evento;
    }

    private void dadoEventos(EventoAnalisis... valores) {
        given(eventos.findAll()).willReturn(List.of(valores));
    }
}
