package com.backend.nutri_predic;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class AuthProfileApiIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @Transactional
    void estabilizaRegistroLoginLogoutPerfilErroresYCors() throws Exception {
        String email = "api-" + UUID.randomUUID() + "@test.local";
        String registerJson =
                """
                {"email":"%s","password":"Password1!","nombre":"Usuario API","sexo":"MASCULINO","realizaActividadFisica":true,"diasEntrenamientoSemana":3,"tipoActividadFisica":"Pesas"}
                """
                        .formatted(email);

        MvcResult register =
                mockMvc.perform(
                                post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(registerJson))
                        .andExpect(status().isCreated())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.token").isNotEmpty())
                        .andExpect(jsonPath("$.tokenType").value("Bearer"))
                        .andExpect(jsonPath("$.email").value(email))
                        .andExpect(jsonPath("$.nombre").value("Usuario API"))
                        .andExpect(jsonPath("$.rol").value("CLIENTE"))
                        .andExpect(jsonPath("$.password").doesNotExist())
                        .andReturn();

        JsonNode auth = objectMapper.readTree(register.getResponse().getContentAsString());
        String token = auth.get("token").asText();
        long clienteId = auth.get("clienteId").asLong();

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("El email ya está registrado"));

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"email":"%s","password":"Password1!"}
                                """
                                                .formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.clienteId").value(clienteId));

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"email":"%s","password":"Password-incorrecta"}
                                """
                                                .formatted(email)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));

        mockMvc.perform(
                        put("/api/clientes/{id}", clienteId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"edad":30,"pesoKg":80.00,"alturaCm":180.00,
                                 "objetivoFisico":"Ganar masa muscular"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imc").value(24.69));

        mockMvc.perform(
                        get("/api/usuarios/me")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(clienteId))
                .andExpect(jsonPath("$.nombre").value("Usuario API"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.edad").value(30))
                .andExpect(jsonPath("$.pesoKg").value(80.0))
                .andExpect(jsonPath("$.alturaCm").value(180.0))
                .andExpect(jsonPath("$.imc").value(24.69))
                .andExpect(jsonPath("$.objetivoFisico").value("Ganar masa muscular"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(
                        post("/api/auth/logout")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"email\":\"incorrecto\",\"password\":\"corta\",\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La solicitud contiene campos inválidos"))
                .andExpect(jsonPath("$.fieldErrors").isArray());

        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Se requiere un token Bearer válido"));

        mockMvc.perform(
                        get("/api/usuarios/me")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(
                        options("/api/auth/login")
                                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                                        "http://localhost:5173"))
                .andExpect(
                        header().string(
                                        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                                        org.hamcrest.Matchers.containsString("POST")));
    }
}
