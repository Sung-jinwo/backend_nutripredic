package com.backend.nutri_predic;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.conocimiento.entity.*;
import com.backend.nutri_predic.conocimiento.repository.InstrumentoConocimientoRepository;
import com.backend.nutri_predic.conocimiento.repository.InstrumentoPreguntaRepository;
import com.backend.nutri_predic.conocimiento.repository.PreguntaConocimientoRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PccInstrumentoIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AuthService auth;
    @Autowired ClienteRepository clientes;
    @Autowired InstrumentoConocimientoRepository instrumentos;
    @Autowired InstrumentoPreguntaRepository instrumentoPreguntas;
    @Autowired PreguntaConocimientoRepository preguntas;

    @Test
    @Transactional
    void instrumentoActivoDevuelveContratoPublicoSinSoluciones() throws Exception {
        var reg =
                auth.register(
                        new RegisterRequest(
                                "pcc-pub-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "PCCPub"));
        var inst = crearInstrumentoActivo("PCC-PUB", 2);
        var p1 =
                crearPregunta(
                        "Proteínas", "PROTEINA", "Subtema-A", "A", "Explicación-A", "Fuente-A");
        var p2 =
                crearPregunta(
                        "Vitaminas", "VITAMINA", "Subtema-B", "B", "Explicación-B", "Fuente-B");
        vincular(inst, p1, 1, new BigDecimal("1"));
        vincular(inst, p2, 2, new BigDecimal("1"));

        mockMvc.perform(
                        get("/api/tests/instrumento-activo")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reg.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(inst.getId()))
                .andExpect(jsonPath("$.codigo").value("PCC-PUB"))
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.nombre").value("Test Activo"))
                .andExpect(jsonPath("$.preguntas").isArray())
                .andExpect(jsonPath("$.preguntas.length()").value(2))
                .andExpect(jsonPath("$.preguntas[0].enunciado").value("Proteínas"))
                .andExpect(jsonPath("$.preguntas[0].respuestaCorrecta").doesNotExist())
                .andExpect(jsonPath("$.preguntas[0].explicacion").doesNotExist())
                .andExpect(jsonPath("$.preguntas[0].fuenteReferencia").doesNotExist())
                .andExpect(jsonPath("$.preguntas[1].respuestaCorrecta").doesNotExist())
                .andExpect(jsonPath("$.preguntas[1].explicacion").doesNotExist())
                .andExpect(jsonPath("$.preguntas[1].fuenteReferencia").doesNotExist());
    }

    @Test
    @Transactional
    void respuestasConInstrumentoConjuntoExactoValidoYEInvalido() throws Exception {
        var reg =
                auth.register(
                        new RegisterRequest(
                                "pcc-exacto-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "PCC"));
        var inst = crearInstrumentoActivo("PCC-EXACTO", 1);
        var p1 = crearPregunta("Q1", "TEMA1", null, "A", null, null);
        var p2 = crearPregunta("Q2", "TEMA2", null, "B", null, null);
        var p3 = crearPregunta("Q3", "TEMA3", null, "C", null, null);
        vincular(inst, p1, 1, new BigDecimal("1"));
        vincular(inst, p2, 2, new BigDecimal("1"));
        vincular(inst, p3, 3, new BigDecimal("1"));

        String validBody =
                """
                {"clienteId":%d,"instrumentoId":%d,"momento":"BASAL","respuestas":[
                  {"preguntaId":%d,"opcion":"A"},
                  {"preguntaId":%d,"opcion":"B"},
                  {"preguntaId":%d,"opcion":"C"}
                ]}
                """
                        .formatted(
                                reg.clienteId(), inst.getId(), p1.getId(), p2.getId(), p3.getId());
        mockMvc.perform(
                        post("/api/tests/respuestas")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reg.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.correctas").value(3))
                .andExpect(jsonPath("$.porcentaje").value(100.0));

        String missingOne =
                """
                {"clienteId":%d,"instrumentoId":%d,"respuestas":[
                  {"preguntaId":%d,"opcion":"A"},
                  {"preguntaId":%d,"opcion":"B"}
                ]}
                """
                        .formatted(reg.clienteId(), inst.getId(), p1.getId(), p2.getId());
        mockMvc.perform(
                        post("/api/tests/respuestas")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reg.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(missingOne))
                .andExpect(status().isBadRequest());

        var pExtra = crearPregunta("QExtra", "TEMA4", null, "D", null, null);
        String extraOne =
                """
                {"clienteId":%d,"instrumentoId":%d,"respuestas":[
                  {"preguntaId":%d,"opcion":"A"},
                  {"preguntaId":%d,"opcion":"B"},
                  {"preguntaId":%d,"opcion":"C"},
                  {"preguntaId":%d,"opcion":"D"}
                ]}
                """
                        .formatted(
                                reg.clienteId(),
                                inst.getId(),
                                p1.getId(),
                                p2.getId(),
                                p3.getId(),
                                pExtra.getId());
        mockMvc.perform(
                        post("/api/tests/respuestas")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reg.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(extraOne))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void basalPersisteMomentoYFeedbackIncluyeSoluciones() throws Exception {
        var reg =
                auth.register(
                        new RegisterRequest(
                                "pcc-basal-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "Basal"));
        var inst = crearInstrumentoActivo("PCC-BASAL", 1);
        var p1 =
                crearPregunta(
                        "Fuente de proteína",
                        "PROTEINA",
                        "Aminoácidos",
                        "A",
                        "Las proteínas contienen aminoácidos",
                        "Guía nutricional 2025");
        var p2 =
                crearPregunta(
                        "Vitamina C",
                        "VITAMINA",
                        "Antioxidantes",
                        "C",
                        "La vitamina C es un antioxidante",
                        "OMS 2024");
        vincular(inst, p1, 1, new BigDecimal("2"));
        vincular(inst, p2, 2, new BigDecimal("3"));

        String body =
                """
                {"clienteId":%d,"instrumentoId":%d,"momento":"BASAL","respuestas":[
                  {"preguntaId":%d,"opcion":"A"},
                  {"preguntaId":%d,"opcion":"A"}
                ]}
                """
                        .formatted(reg.clienteId(), inst.getId(), p1.getId(), p2.getId());
        MvcResult result =
                mockMvc.perform(
                                post("/api/tests/respuestas")
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + reg.token())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.momento").value("BASAL"))
                        .andExpect(jsonPath("$.instrumentoId").value(inst.getId()))
                        .andExpect(jsonPath("$.estadoValidez").value("VALIDA"))
                        .andExpect(jsonPath("$.correctas").value(1))
                        .andExpect(jsonPath("$.resultadosPorTema").isArray())
                        .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        long testId = response.get("id").asLong();

        mockMvc.perform(
                        get("/api/clientes/{clienteId}/tests/{id}", reg.clienteId(), testId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reg.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.momento").value("BASAL"))
                .andExpect(jsonPath("$.respuestas.length()").value(2))
                .andExpect(jsonPath("$.respuestas[0].respuestaCorrecta").exists())
                .andExpect(jsonPath("$.respuestas[0].correcta").isBoolean())
                .andExpect(jsonPath("$.respuestas[0].explicacion").exists())
                .andExpect(jsonPath("$.respuestas[0].fuente").exists())
                .andExpect(jsonPath("$.respuestas[0].subtema").exists());
    }

    @Test
    @Transactional
    void legacySinInstrumentoIdSigueFuncionando() throws Exception {
        var reg =
                auth.register(
                        new RegisterRequest(
                                "pcc-legacy-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "Legacy"));
        var p1 = crearPregunta("Legacy Q1", "GENERAL", null, "B", null, null);

        String body =
                """
                {"clienteId":%d,"respuestas":[{"preguntaId":%d,"opcion":"B"}]}
                """
                        .formatted(reg.clienteId(), p1.getId());
        mockMvc.perform(
                        post("/api/tests/respuestas")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reg.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.correctas").value(1));
    }

    private InstrumentoConocimiento crearInstrumentoActivo(String codigo, int version) {
        var inst = new InstrumentoConocimiento();
        inst.setCodigo(codigo);
        inst.setVersion(version);
        inst.setNombre("Test Activo");
        inst.setEstado(EstadoInstrumento.ACTIVO);
        inst.setVigenteDesde(Instant.now());
        inst.setFuenteReferencia("OMS 2024");
        return instrumentos.save(inst);
    }

    private PreguntaConocimiento crearPregunta(
            String enunciado,
            String tema,
            String subtema,
            String correcta,
            String explicacion,
            String fuente) {
        var p = new PreguntaConocimiento();
        p.setEnunciado(enunciado);
        p.setOpcionA("Opción A");
        p.setOpcionB("Opción B");
        p.setOpcionC("Opción C");
        p.setOpcionD("Opción D");
        p.setRespuestaCorrecta(correcta);
        p.setTema(tema);
        p.setCategoria(tema);
        p.setSubtema(subtema);
        p.setDificultad("BASICA");
        p.setExplicacion(explicacion);
        p.setFuenteReferencia(fuente);
        return preguntas.save(p);
    }

    private void vincular(
            InstrumentoConocimiento inst,
            PreguntaConocimiento p,
            int orden,
            BigDecimal puntuacion) {
        var ip = new InstrumentoPregunta();
        ip.setInstrumento(inst);
        ip.setPregunta(p);
        ip.setOrden(orden);
        ip.setPuntuacion(puntuacion);
        instrumentoPreguntas.save(ip);
    }
}
