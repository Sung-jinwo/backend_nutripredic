package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.conocimiento.practica.service.GeneracionPreguntasConocimientoService;
import com.backend.nutri_predic.conocimiento.practica.service.RespuestaConocimientoIaService;
import com.backend.nutri_predic.conocimiento.practica.repository.*;
import com.backend.nutri_predic.conocimiento.practica.dto.ResponderConocimientoIaRequest;
import com.backend.nutri_predic.plandia.entity.*;
import com.backend.nutri_predic.plandia.repository.PlanDiarioRepository;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest(properties="app.gemini.mock-enabled=true")
class ConocimientoInicialIntegrationTests {
    @Autowired AuthService auth;
    @Autowired ClienteRepository clientes;
    @Autowired PlanDiarioRepository planes;
    @Autowired GeneracionPreguntasConocimientoService generacion;
    @Autowired RespuestaConocimientoIaService responder;
    @Autowired PreguntaGeneradaIaRepository preguntas;
    @Autowired SesionConocimientoIaRepository sesiones;

    @Test @Transactional
    void perfilNuevoTieneCincoPreguntasSinPrediccionFicticiaYUnaSolaEntrega() {
        var registro=auth.register(new RegisterRequest("inicial-"+UUID.randomUUID()+"@test.local","Password1!","Inicial"));
        var cliente=clientes.findById(registro.clienteId()).orElseThrow();
        cliente.setObjetivoFisico("Mantener peso");
        var plan=new PlanDiario(); plan.setCliente(cliente); plan.setFechaObjetivo(LocalDate.now()); plan.setEstado(EstadoPlanDiario.DISPONIBLE);
        plan.setEnergiaMaxKcal(new BigDecimal("2000")); plan.setProteinaMaxG(new BigDecimal("100"));
        plan.setCarbohidratosMaxG(new BigDecimal("250")); plan.setGrasasMaxG(new BigDecimal("66.67")); plan.setAguaMaxMl(new BigDecimal("3000"));
        plan=planes.saveAndFlush(plan);
        var primera=generacion.generarInicialDesdePlan(cliente,plan);
        assertThat(primera.preguntasAdaptativas()).hasSize(5);
        assertThat(primera.clasificacionPredictiva()).isNull();
        assertThat(sesiones.findById(primera.sesionId()).orElseThrow().getPrediccionModelo()).isNull();
        assertThat(generacion.generarInicialDesdePlan(cliente,plan).sesionId()).isEqualTo(primera.sesionId());
        var entrega=preguntas.findBySesionIdOrderByOrdenAsc(primera.sesionId()).stream()
            .map(q->new ResponderConocimientoIaRequest.Respuesta(q.getId(),q.getRespuestaCorrecta().equals("A")?"B":"A")).toList();
        var admin=new UsernamePasswordAuthenticationToken("admin@test.local","",List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        var resultado=responder.responder(cliente.getId(),primera.sesionId(),new ResponderConocimientoIaRequest(entrega),admin);
        assertThat(resultado.puntajeObtenido()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultado.nivel()).isEqualTo("BAJO");
        assertThat(sesiones.findById(primera.sesionId()).orElseThrow().getEstadoValidez().name()).isEqualTo("INVALIDA");
    }
}
