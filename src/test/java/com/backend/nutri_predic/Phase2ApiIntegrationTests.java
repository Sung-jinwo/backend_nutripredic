package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.backend.nutri_predic.common.enums.NivelConocimiento;
import com.backend.nutri_predic.conocimiento.entity.PreguntaConocimiento;
import com.backend.nutri_predic.conocimiento.repository.PreguntaConocimientoRepository;
import com.backend.nutri_predic.conocimiento.evaluacion.repository.ResultadoTestRepository;
import com.backend.nutri_predic.suplemento.entity.SuplementoCatalogo;
import com.backend.nutri_predic.suplemento.repository.SuplementoCatalogoRepository;
import java.time.LocalDate;
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
class Phase2ApiIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired SuplementoCatalogoRepository catalogoRepository;
    @Autowired PreguntaConocimientoRepository preguntaRepository;
    @Autowired ResultadoTestRepository resultados;

    @Test
    @Transactional
    void creaListaActualizaHabitosYBloqueaAccesoEntreClientes() throws Exception {
        Auth cliente = register("habito-a");
        Auth intruso = register("habito-b");
        String fecha = LocalDate.now().minusDays(2).toString();
        String createBody =
                """
                {"clienteId":%d,"fecha":"%s","cantidadComidas":4,"consumoAgua":2.5,
                 "proteinas":120.5,"tipoAlimentacion":"Omnívora","nivelOrganizacion":"ALTO",
                 "desayuno":true,"snacks":false,"alimentos":"pollo, arroz y verduras",
                 "comidasCocinadas":3,"restricciones":"lactosa","consumeSuplementos":true}
                """
                        .formatted(cliente.clienteId(), fecha);

        MvcResult created =
                mockMvc.perform(
                                post("/api/habitos")
                                        .headers(auth(cliente))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(createBody))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.clienteId").value(cliente.clienteId()))
                        .andExpect(jsonPath("$.alimentos").value("pollo, arroz y verduras"))
                        .andExpect(jsonPath("$.comidasCocinadas").value(3))
                        .andExpect(jsonPath("$.consumeSuplementos").value(true))
                        .andReturn();
        long habitoId = json(created).get("id").asLong();

        mockMvc.perform(
                        get("/api/habitos/cliente/{id}", cliente.clienteId())
                                .headers(auth(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(habitoId));
        mockMvc.perform(
                        get("/api/habitos/cliente/{id}", cliente.clienteId())
                                .headers(auth(intruso)))
                .andExpect(status().isForbidden());
        mockMvc.perform(
                        put("/api/habitos/{id}", habitoId)
                                .headers(auth(intruso))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        createBody.replace(
                                                "\"clienteId\":" + cliente.clienteId() + ",", "")))
                .andExpect(status().isForbidden());

        String nuevaFecha = LocalDate.now().minusDays(1).toString();
        String updateBody =
                """
                {"fecha":"%s","cantidadComidas":5,"consumoAgua":3.0,"proteinas":140.0,
                 "tipoAlimentacion":"Mediterránea","nivelOrganizacion":"MEDIO","desayuno":false,
                 "snacks":true,"alimentos":"pescado y legumbres","comidasCocinadas":4,
                 "restricciones":"ninguna","consumeSuplementos":false}
                """
                        .formatted(nuevaFecha);
        mockMvc.perform(
                        put("/api/habitos/{id}", habitoId)
                                .headers(auth(cliente))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value(nuevaFecha))
                .andExpect(jsonPath("$.alimentos").value("pescado y legumbres"))
                .andExpect(jsonPath("$.comidasCocinadas").value(4))
                .andExpect(jsonPath("$.consumeSuplementos").value(false));
        mockMvc.perform(
                        get("/api/habitos/cliente/{id}/{fecha}", cliente.clienteId(), nuevaFecha)
                                .headers(auth(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadComidas").value(5));
    }

    @Test
    @Transactional
    void registraListaYEditaSuplementos() throws Exception {
        Auth cliente = register("suplemento");
        SuplementoCatalogo suplemento = new SuplementoCatalogo();
        suplemento.setNombre("Creatina " + UUID.randomUUID());
        suplemento.setTipo("Rendimiento");
        suplemento.setDescripcion("Monohidrato");
        suplemento = catalogoRepository.save(suplemento);
        String inicio = LocalDate.now().minusDays(10).toString();
        String fin = LocalDate.now().plusMonths(2).toString();
        String body =
                """
                {"suplementoId":%d,"nombreSuplemento":"Creatina de prueba","cantidad":5.0,"unidad":"g","frecuencia":"diaria",
                 "tiempoUso":"8 semanas","activo":true,"fechaInicio":"%s","fechaFin":"%s"}
                """
                        .formatted(suplemento.getId(), inicio, fin);
        mockMvc.perform(
                        post("/api/clientes/{id}/suplementos", cliente.clienteId())
                                .headers(auth(cliente))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.suplementoId").value(suplemento.getId()))
                .andExpect(jsonPath("$.tiempoUso").value("8 semanas"));
        mockMvc.perform(
                        get("/api/clientes/{id}/suplementos", cliente.clienteId())
                                .headers(auth(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cantidad").value(5.0));
        String update =
                """
                {"nombreSuplemento":"Creatina de prueba","cantidad":3.0,"unidad":"g","frecuencia":"días de entrenamiento",
                 "tiempoUso":"12 semanas","activo":false,"fechaInicio":"%s","fechaFin":"%s"}
                """
                        .formatted(inicio, fin);
        mockMvc.perform(
                        put(
                                        "/api/clientes/{clienteId}/suplementos/{suplementoId}",
                                        cliente.clienteId(),
                                        suplemento.getId())
                                .headers(auth(cliente))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(3.0))
                .andExpect(jsonPath("$.tiempoUso").value("12 semanas"))
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    @Transactional
    void realizaTestOcultaRespuestaYRegistraNivelEnResultado() throws Exception {
        Auth cliente = register("test");
        PreguntaConocimiento p1 = question("Proteína", "A");
        PreguntaConocimiento p2 = question("Agua", "B");
        PreguntaConocimiento p3 = question("Vitaminas", "C");

        mockMvc.perform(get("/api/preguntas").headers(auth(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].respuestaCorrecta").doesNotExist());
        String body =
                """
                {"clienteId":%d,"respuestas":[{"preguntaId":%d,"opcion":"A"},
                 {"preguntaId":%d,"opcion":"A"},{"preguntaId":%d,"opcion":"A"}]}
                """
                        .formatted(cliente.clienteId(), p1.getId(), p2.getId(), p3.getId());
        mockMvc.perform(
                        post("/api/tests/respuestas")
                                .headers(auth(cliente))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.correctas").value(1))
                .andExpect(jsonPath("$.nivel").value("BAJO"));
        mockMvc.perform(get("/api/clientes/{id}/tests", cliente.clienteId()).headers(auth(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nivel").value("BAJO"));

        // El nivel oficial vive en resultados_tests (no en Cliente): 1 BAJO de 1 total.
        // (IndicadorService legacy eliminado: PCC oficial vía PccIndicatorService.)
        assertThat(resultados.countByNivel(NivelConocimiento.BAJO)).isEqualTo(1);
    }

    private PreguntaConocimiento question(String text, String answer) {
        var p = new PreguntaConocimiento();
        p.setEnunciado(text);
        p.setOpcionA("A");
        p.setOpcionB("B");
        p.setOpcionC("C");
        p.setOpcionD("D");
        p.setRespuestaCorrecta(answer);
        p.setCategoria("General");
        p.setDificultad("BÁSICA");
        return preguntaRepository.save(p);
    }

    private Auth register(String prefix) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@test.local";
        MvcResult result =
                mockMvc.perform(
                                post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"email\":\"%s\",\"password\":\"Password1!\",\"nombre\":\"Fase 2\"}"
                                                        .formatted(email)))
                        .andExpect(status().isCreated())
                        .andReturn();
        JsonNode json = json(result);
        return new Auth(json.get("clienteId").asLong(), json.get("token").asText());
    }

    private HttpHeaders auth(Auth auth) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(auth.token());
        return h;
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private record Auth(long clienteId, String token) {}
}
