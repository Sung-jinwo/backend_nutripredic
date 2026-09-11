package com.backend.nutri_predic;

import static org.junit.jupiter.api.Assertions.*;

import com.backend.nutri_predic.auth.dto.LoginRequest;
import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.common.enums.NivelConocimiento;
import com.backend.nutri_predic.common.exception.BusinessException;
import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.conocimiento.entity.PreguntaConocimiento;
import com.backend.nutri_predic.conocimiento.repository.PreguntaConocimientoRepository;
import com.backend.nutri_predic.conocimiento.evaluacion.dto.ResponderTestRequest;
import com.backend.nutri_predic.conocimiento.evaluacion.service.TestService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class NutriPredicApplicationTests {
    @Autowired AuthService authService;
    @Autowired AccessService accessService;
    @Autowired PreguntaConocimientoRepository preguntas;
    @Autowired TestService testService;

    @Test
    void contextLoads() {}

    @Test
    @Transactional
    void registraAutenticaYAislaDatosDeClientes() {
        var primero =
                authService.register(new RegisterRequest("uno@test.local", "Password1!", "Uno"));
        var segundo =
                authService.register(new RegisterRequest("dos@test.local", "Password1!", "Dos"));
        var login = authService.login(new LoginRequest("uno@test.local", "Password1!"));
        assertNotNull(login.token());
        assertEquals(primero.clienteId(), login.clienteId());
        var auth =
                UsernamePasswordAuthenticationToken.authenticated(
                        "uno@test.local",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
        assertEquals(primero.clienteId(), accessService.client(primero.clienteId(), auth).getId());
        assertThrows(
                AccessDeniedException.class, () -> accessService.client(segundo.clienteId(), auth));
        assertThrows(
                BusinessException.class,
                () ->
                        authService.register(
                                new RegisterRequest("uno@test.local", "Password1!", "Duplicado")));
    }

    @Test
    @Transactional
    void calculaResultadoYRegistraNivelEnResultado() {
        var registro =
                authService.register(new RegisterRequest("test@test.local", "Password1!", "Test"));
        var pregunta = new PreguntaConocimiento();
        pregunta.setEnunciado("¿Cuál es una fuente de proteína?");
        pregunta.setOpcionA("Pollo");
        pregunta.setOpcionB("Azúcar");
        pregunta.setOpcionC("Agua");
        pregunta.setOpcionD("Sal");
        pregunta.setRespuestaCorrecta("A");
        pregunta = preguntas.save(pregunta);
        var auth =
                UsernamePasswordAuthenticationToken.authenticated(
                        "test@test.local",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
        var resultado =
                testService.submit(
                        new ResponderTestRequest(
                                registro.clienteId(),
                                List.of(new ResponderTestRequest.Respuesta(pregunta.getId(), "A"))),
                        auth);
        assertEquals(100.0, resultado.porcentaje());
        assertEquals(NivelConocimiento.ALTO.name(), resultado.nivel());
    }
}
