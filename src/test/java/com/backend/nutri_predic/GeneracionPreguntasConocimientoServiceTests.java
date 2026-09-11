package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.conocimiento.entity.EstadoInstrumento;
import com.backend.nutri_predic.conocimiento.repository.InstrumentoConocimientoRepository;
import com.backend.nutri_predic.conocimiento.repository.InstrumentoPreguntaRepository;
import com.backend.nutri_predic.conocimiento.gemini.client.GeminiClient;
import com.backend.nutri_predic.conocimiento.gemini.client.GeminiClientException;
import com.backend.nutri_predic.conocimiento.gemini.config.GeminiProperties;
import com.backend.nutri_predic.conocimiento.practica.dto.GenerarConocimientoIaRequest;
import com.backend.nutri_predic.conocimiento.practica.entity.EstadoSesionConocimientoIa;
import com.backend.nutri_predic.conocimiento.practica.entity.SesionConocimientoIa;
import com.backend.nutri_predic.conocimiento.practica.repository.PreguntaGeneradaIaRepository;
import com.backend.nutri_predic.conocimiento.practica.repository.RespuestaAdaptativaIaRepository;
import com.backend.nutri_predic.conocimiento.practica.repository.SesionConocimientoIaRepository;
import com.backend.nutri_predic.conocimiento.gemini.repository.TrazaLlamadaGeminiRepository;
import com.backend.nutri_predic.conocimiento.practica.service.GeneracionPreguntasConocimientoService;
import com.backend.nutri_predic.prediccionmodelo.entity.EstadoPrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GeneracionPreguntasConocimientoServiceTests {
    @Test
    void errorDelClientePersisteEstadoCodigoYHttpSinPropagarlo() {
        var fixture = fixture();
        when(fixture.gemini.generar(any()))
                .thenThrow(
                        new GeminiClientException(
                                "GEMINI_HTTP_429",
                                429,
                                null,
                                "RECEIVE_RESPONSE",
                                "HttpStatusException"));

        var respuesta = fixture.service.generar(10L, request(), null);

        var captor = ArgumentCaptor.forClass(SesionConocimientoIa.class);
        verify(fixture.sesiones).save(captor.capture());
        var sesion = captor.getValue();
        assertThat(respuesta.estadoAdaptativo()).isEqualTo("IA_NO_DISPONIBLE");
        assertThat(sesion.getEstado()).isEqualTo(EstadoSesionConocimientoIa.IA_NO_DISPONIBLE);
        assertThat(sesion.getCodigoErrorTecnico()).isEqualTo("GEMINI_HTTP_429");
        assertThat(sesion.getHttpStatusGemini()).isEqualTo(429);
        assertThat(sesion.getFallidoEn()).isNotNull();
        assertThat(sesion.getEtapaError()).isEqualTo("RECEIVE_RESPONSE");
        assertThat(sesion.getTipoExcepcionSeguro()).isEqualTo("HttpStatusException");
    }

    @Test
    void contenidoLocalmenteInvalidoPersisteCodigoDeValidacion() {
        var fixture = fixture();
        when(fixture.gemini.generar(any()))
                .thenReturn(
                        List.of(
                                pregunta("NO_PERMITIDO", "MEDIA", 4),
                                pregunta("NO_PERMITIDO", "MEDIA", 4)));

        fixture.service.generar(10L, request(), null);

        var captor = ArgumentCaptor.forClass(SesionConocimientoIa.class);
        verify(fixture.sesiones).save(captor.capture());
        assertThat(captor.getValue().getCodigoErrorTecnico()).isEqualTo("GEMINI_TOPIC_NOT_ALLOWED");
        assertThat(captor.getValue().getEstado())
                .isEqualTo(EstadoSesionConocimientoIa.IA_NO_DISPONIBLE);
    }

    @Test
    void cantidadDificultadYOpcionesInvalidasTienenCodigosDistintos() {
        assertCodigo(List.of(pregunta("PROTEINA", "MEDIA", 4)), "GEMINI_QUESTION_COUNT_INVALID");
        assertCodigo(
                List.of(pregunta("PROTEINA", "ALTA", 4), pregunta("PROTEINA", "ALTA", 4)),
                "GEMINI_DIFFICULTY_INVALID");
        assertCodigo(
                List.of(pregunta("PROTEINA", "MEDIA", 3), pregunta("PROTEINA", "MEDIA", 3)),
                "GEMINI_OPTIONS_INVALID");
    }

    private void assertCodigo(
            List<GeminiClient.Respuesta.Pregunta> respuestaGemini, String codigo) {
        var fixture = fixture();
        when(fixture.gemini.generar(any())).thenReturn(respuestaGemini);
        fixture.service.generar(10L, request(), null);
        var captor = ArgumentCaptor.forClass(SesionConocimientoIa.class);
        verify(fixture.sesiones).save(captor.capture());
        assertThat(captor.getValue().getCodigoErrorTecnico()).isEqualTo(codigo);
    }

    private Fixture fixture() {
        var predicciones = mock(PrediccionModeloRepository.class);
        var sesiones = mock(SesionConocimientoIaRepository.class);
        var preguntas = mock(PreguntaGeneradaIaRepository.class);
        var respuestas = mock(RespuestaAdaptativaIaRepository.class);
        var trazas = mock(TrazaLlamadaGeminiRepository.class);
        var instrumentos = mock(InstrumentoConocimientoRepository.class);
        var instrumentoPreguntas = mock(InstrumentoPreguntaRepository.class);
        var gemini = mock(GeminiClient.class);
        var access = mock(AccessService.class);
        var planes = mock(com.backend.nutri_predic.plandia.repository.PlanDiarioRepository.class);
        var prediccion = mock(PrediccionModelo.class);
        var cliente = mock(com.backend.nutri_predic.cliente.entity.Cliente.class);
        when(prediccion.getId()).thenReturn(20L);
        when(prediccion.getCliente()).thenReturn(cliente);
        when(cliente.getId()).thenReturn(10L);
        when(prediccion.getSchemaVersion()).thenReturn("variables-modelo-v5");
        when(prediccion.getModelVersion()).thenReturn("v5-test");
        when(predicciones.findByClienteIdAndEstadoOrderByFechaPrediccionDesc(
                        10L, EstadoPrediccionModelo.EXITOSA))
                .thenReturn(List.of(prediccion));
        when(sesiones.findByPrediccionModeloIdAndConfiguracionVersion(20L, "pcc-ia-v1"))
                .thenReturn(Optional.empty());
        when(sesiones.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(instrumentos.findFirstByEstadoOrderByVigenteDesdeDescIdDesc(EstadoInstrumento.ACTIVO))
                .thenReturn(Optional.empty());
        when(preguntas.findBySesionIdOrderByOrdenAsc(any())).thenReturn(List.of());
        var properties = new GeminiProperties();
        properties.setModel("gemini-test");
        return new Fixture(
                new GeneracionPreguntasConocimientoService(
                        predicciones,
                        sesiones,
                        preguntas,
                        respuestas,
                        trazas,
                        instrumentos,
                        instrumentoPreguntas,
                        gemini,
                        properties,
                        access,
                        planes),
                sesiones,
                gemini);
    }

    private GenerarConocimientoIaRequest request() {
        return new GenerarConocimientoIaRequest(List.of("PROTEINA"), "MEDIA", 2);
    }

    private GeminiClient.Respuesta.Pregunta pregunta(String tema, String dificultad, int opciones) {
        var lista =
                List.of(
                                new GeminiClient.Opcion("A", "A"),
                                new GeminiClient.Opcion("B", "B"),
                                new GeminiClient.Opcion("C", "C"),
                                new GeminiClient.Opcion("D", "D"))
                        .subList(0, opciones);
        return new GeminiClient.Respuesta.Pregunta(
                tema, "General", dificultad, "Enunciado", lista, "B", "Explicacion");
    }

    private record Fixture(
            GeneracionPreguntasConocimientoService service,
            SesionConocimientoIaRepository sesiones,
            GeminiClient gemini) {}
}
