package com.backend.nutri_predic.conocimiento.practica.service;

import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.plandia.service.PlanDiarioService;
import com.backend.nutri_predic.plandia.repository.PlanDiarioRepository;
import com.backend.nutri_predic.conocimiento.practica.dto.SesionConocimientoPublicaResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConocimientoInicialService {
    private final ClienteRepository clientes;
    private final PlanDiarioService planes;
    private final PlanDiarioRepository repositorioPlanes;
    private final GeneracionPreguntasConocimientoService preguntas;
    private final AccessService access;
    public ConocimientoInicialService(ClienteRepository clientes, PlanDiarioService planes,
            PlanDiarioRepository repositorioPlanes, GeneracionPreguntasConocimientoService preguntas, AccessService access) {
        this.clientes = clientes; this.planes = planes; this.repositorioPlanes = repositorioPlanes; this.preguntas = preguntas; this.access = access;
    }
    @Transactional
    public SesionConocimientoPublicaResponse asegurar(Long clienteId, Authentication auth) {
        return asegurar(clienteId, auth, false);
    }
    @Transactional
    public SesionConocimientoPublicaResponse asegurarDiaria(Long clienteId, Authentication auth) {
        return asegurar(clienteId, auth, true);
    }
    private SesionConocimientoPublicaResponse asegurar(Long clienteId, Authentication auth, boolean diaria) {
        access.client(clienteId, auth);
        var cliente = clientes.findByIdForUpdate(clienteId).orElseThrow(() -> new ResourceNotFoundException("Cliente"));
        var hoy = LocalDate.now(ZoneId.of("America/Lima"));
        planes.generarInicial(clienteId, hoy);
        var plan = repositorioPlanes.findByClienteIdAndFechaObjetivo(clienteId, hoy)
                .orElseThrow(() -> new ResourceNotFoundException("Completa el perfil para disponer de metas y preguntas educativas"));
        if (plan.getEstado() != com.backend.nutri_predic.plandia.entity.EstadoPlanDiario.DISPONIBLE)
            throw new ResourceNotFoundException("Las metas todavía no están disponibles; completa los datos esenciales del perfil");
        return diaria ? preguntas.generarDiariaDesdePlan(cliente, plan) : preguntas.generarInicialDesdePlan(cliente, plan);
    }
}
