package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.conocimiento.practica.repository.SesionConocimientoIaRepository;
import com.backend.nutri_predic.consumo.repository.EvaluacionConsumoRepository;
import com.backend.nutri_predic.perfilhabitos.entity.ClasificacionPerfilHabitos;
import com.backend.nutri_predic.prediccionmodelo.ciclo.service.CicloDiarioService;
import com.backend.nutri_predic.prediccionmodelo.entity.EstadoPrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.*;
import com.backend.nutri_predic.prediccionmodelo.evento.repository.EventoAnalisisRepository;
import com.backend.nutri_predic.prediccionmodelo.evento.service.EventoAnalisisLifecycleService;
import com.backend.nutri_predic.prediccionmodelo.evento.service.EventoAnalisisService;
import com.backend.nutri_predic.prediccionmodelo.post.CicloPostPrediccionV5Service;
import com.backend.nutri_predic.prediccionmodelo.post.EstadoCicloPostPrediccion;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import com.backend.nutri_predic.prediccionmodelo.service.ModeloPredictivoV6Service;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class CicloDiarioServiceTests {
    private final AccessService access = mock(AccessService.class);
    private final PrediccionModeloRepository predicciones = mock(PrediccionModeloRepository.class);
    private final EventoAnalisisRepository eventos = mock(EventoAnalisisRepository.class);
    private final EventoAnalisisService analisis = mock(EventoAnalisisService.class);
    private final CicloPostPrediccionV5Service post = mock(CicloPostPrediccionV5Service.class);
    private final ModeloPredictivoV6Service modelo = mock(ModeloPredictivoV6Service.class);
    private final EventoAnalisisLifecycleService lifecycle = mock(EventoAnalisisLifecycleService.class);
    private final SesionConocimientoIaRepository sesiones = mock(SesionConocimientoIaRepository.class);
    private final EvaluacionConsumoRepository evaluaciones = mock(EvaluacionConsumoRepository.class);
    private final Authentication auth = mock(Authentication.class);
    private final CicloDiarioService service = new CicloDiarioService(
            access, predicciones, eventos, analisis, post, modelo, lifecycle, sesiones, evaluaciones);

    private PrediccionModelo prediccion;
    private EventoAnalisis eventoFallido;

    @BeforeEach
    void setUp() {
        prediccion = mock(PrediccionModelo.class);
        when(prediccion.getId()).thenReturn(50L);
        when(prediccion.getFechaCorte()).thenReturn(LocalDate.now(java.time.ZoneId.of("America/Lima")));
        when(prediccion.getMomentoEvaluacion()).thenReturn(MomentoEvaluacion.DIARIO);
        when(prediccion.getModelVersion()).thenReturn(ModeloPredictivoV6Service.MODEL_VERSION_ESPERADA);
        when(prediccion.getClasificacionPredicha()).thenReturn(ClasificacionPerfilHabitos.MEJORABLE);
        when(predicciones.findFirstByClienteIdAndFechaCorteAndMomentoEvaluacionAndSchemaVersionAndEstadoOrderByFechaPrediccionDesc(
                eq(1L), any(LocalDate.class), eq(MomentoEvaluacion.DIARIO),
                eq("variables-modelo-v6"), eq(EstadoPrediccionModelo.EXITOSA)))
                .thenReturn(Optional.of(prediccion));

        eventoFallido = mock(EventoAnalisis.class);
        when(eventoFallido.getId()).thenReturn(70L);
        when(eventoFallido.getEstadoCicloDiario()).thenReturn(EstadoCicloDiario.FALLIDO);
        when(eventoFallido.getOrigenResultado()).thenReturn(OrigenResultadoAnalisis.GENERADO);
        when(eventoFallido.getEstadoValidez()).thenReturn(EstadoValidezMedicion.VALIDA);
        when(eventoFallido.getModuloFalloCiclo()).thenReturn("PCC_IA");
        when(eventos.findFirstByPrediccionModeloIdAndOrigenResultadoOrderByIdAsc(
                50L, OrigenResultadoAnalisis.GENERADO)).thenReturn(Optional.of(eventoFallido));
    }

    @Test
    void reintentoExitosoReutilizaPrediccionYNoEjecutaOtraInferencia() {
        var completado = mock(EventoAnalisis.class);
        when(completado.getId()).thenReturn(70L);
        when(completado.getEstadoCicloDiario()).thenReturn(EstadoCicloDiario.COMPLETADO);
        when(completado.getOrigenResultado()).thenReturn(OrigenResultadoAnalisis.GENERADO);
        when(completado.getEstadoValidez()).thenReturn(EstadoValidezMedicion.VALIDA);
        when(completado.getProcesamientoCicloMs()).thenReturn(125L);
        when(post.procesar(prediccion)).thenReturn(EstadoCicloPostPrediccion.completo("GENERADA", "NO_ALTO"));
        when(lifecycle.registrarIntentoCiclo(eq(70L), anyLong(), eq(true), isNull(), isNull()))
                .thenReturn(completado);

        var respuesta = service.asegurar(1L, auth);

        assertThat(respuesta.estado()).isEqualTo("COMPLETADO");
        assertThat(respuesta.reutilizado()).isTrue();
        assertThat(respuesta.prediccionId()).isEqualTo(50L);
        verify(analisis, never()).predecirInstrumentadoV6(any(), any());
        verify(modelo, never()).preparacion(anyLong(), any());
        verify(post).procesar(prediccion);
    }

    @Test
    void falloDeGeminiQuedaFueraDelCierreYDisponibleParaOtroReintento() {
        when(post.procesar(prediccion)).thenReturn(
                EstadoCicloPostPrediccion.fallido("NO_DISPONIBLE", "NO_DETERMINADA", "PCC_IA", "Gemini no disponible"));
        when(lifecycle.registrarIntentoCiclo(eq(70L), anyLong(), eq(false), eq("PCC_IA"), anyString()))
                .thenReturn(eventoFallido);

        var respuesta = service.asegurar(1L, auth);

        assertThat(respuesta.estado()).isEqualTo("FALLIDO");
        assertThat(respuesta.reutilizado()).isTrue();
        assertThat(respuesta.analisis().estadoCicloDiario()).isEqualTo("FALLIDO");
        verify(analisis, never()).predecirInstrumentadoV6(any(), any());
    }
}
