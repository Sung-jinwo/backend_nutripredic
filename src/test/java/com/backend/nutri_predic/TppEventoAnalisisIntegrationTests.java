package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.ProcedimientoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.TipoProcedimientoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.repository.ProcedimientoAnalisisRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class TppEventoAnalisisIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AuthService auth;
    @Autowired ProcedimientoAnalisisRepository procedimientos;

    @Test
    void procedimientoManualIniciarFinalizarYOrdenTemporal() throws Exception {
        var reg =
                auth.register(
                        new RegisterRequest(
                                "tpp-manual-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "Manual"));
        var proc =
                crearProcedimiento(
                        "MANUAL-V1", 1, TipoProcedimientoAnalisis.MANUAL, "Evaluación manual");

        String body =
                "{\"clienteId\":%d,\"procedimientoId\":%d,\"fechaCorte\":\"2026-08-27\",\"momento\":\"BASAL\"}"
                        .formatted(reg.clienteId(), proc.getId());
        MvcResult result =
                mockMvc.perform(
                                post("/api/analisis")
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + reg.token())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").isNumber())
                        .andExpect(jsonPath("$.clienteId").value(reg.clienteId()))
                        .andExpect(jsonPath("$.momento").value("BASAL"))
                        .andExpect(jsonPath("$.procedimientoTipo").value("MANUAL"))
                        .andExpect(jsonPath("$.procedimientoCodigo").value("MANUAL-V1"))
                        .andExpect(jsonPath("$.inicio").exists())
                        .andExpect(jsonPath("$.fin").doesNotExist())
                        .andReturn();

        JsonNode created = objectMapper.readTree(result.getResponse().getContentAsString());
        long eventoId = created.get("id").asLong();
        assertThat(created.get("tppTotalMs").isNull()).isTrue();

        MvcResult finalResult =
                mockMvc.perform(
                                patch("/api/analisis/{id}/finalizar", eventoId)
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + reg.token())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"observacionesTecnicas\":\"Análisis manual completado\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.momento").value("BASAL"))
                        .andExpect(jsonPath("$.procedimientoTipo").value("MANUAL"))
                        .andExpect(jsonPath("$.procedimientoCodigo").value("MANUAL-V1"))
                        .andExpect(jsonPath("$.estadoValidez").value("VALIDA"))
                        .andExpect(jsonPath("$.fin").exists())
                        .andExpect(jsonPath("$.tppTotalMs").isNumber())
                        .andReturn();

        JsonNode finalized = objectMapper.readTree(finalResult.getResponse().getContentAsString());
        long tpp = finalized.get("tppTotalMs").asLong();
        assertThat(tpp).isGreaterThanOrEqualTo(0);

        mockMvc.perform(
                        patch("/api/analisis/{id}/finalizar", eventoId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reg.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"observacionesTecnicas\":\"duplicado\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void procedimientoSoftwareIaEsRepresentable() throws Exception {
        var reg =
                auth.register(
                        new RegisterRequest(
                                "tpp-ia-" + UUID.randomUUID() + "@test.local", "Password1!", "IA"));
        var proc =
                crearProcedimiento(
                        "SOFTWARE-IA-V1",
                        1,
                        TipoProcedimientoAnalisis.SOFTWARE_IA,
                        "Modelo predictivo");

        String body =
                "{\"clienteId\":%d,\"procedimientoId\":%d,\"fechaCorte\":\"2026-08-27\",\"momento\":\"FINAL\"}"
                        .formatted(reg.clienteId(), proc.getId());
        mockMvc.perform(
                        post("/api/analisis")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reg.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.momento").value("FINAL"))
                .andExpect(jsonPath("$.procedimientoTipo").value("SOFTWARE_IA"))
                .andExpect(jsonPath("$.procedimientoCodigo").value("SOFTWARE-IA-V1"))
                .andExpect(jsonPath("$.inicio").exists());
    }

    @Test
    void procedimientoInactivoEsRechazado() throws Exception {
        var reg =
                auth.register(
                        new RegisterRequest(
                                "tpp-inactivo-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "Inactivo"));
        var proc =
                crearProcedimiento("INACTIVO-V1", 1, TipoProcedimientoAnalisis.MANUAL, "Inactivo");
        proc.setActivo(false);
        procedimientos.save(proc);

        String body =
                "{\"clienteId\":%d,\"procedimientoId\":%d,\"fechaCorte\":\"2026-08-27\",\"momento\":\"NO_DETERMINADO\"}"
                        .formatted(reg.clienteId(), proc.getId());
        mockMvc.perform(
                        post("/api/analisis")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reg.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isBadRequest());
    }

    private ProcedimientoAnalisis crearProcedimiento(
            String codigo, int version, TipoProcedimientoAnalisis tipo, String nombre) {
        var p = new ProcedimientoAnalisis();
        p.setCodigo(codigo);
        p.setVersion(version);
        p.setTipo(tipo);
        p.setNombre(nombre);
        p.setActivo(true);
        return procedimientos.save(p);
    }
}
