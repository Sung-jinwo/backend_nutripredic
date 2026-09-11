package com.backend.nutri_predic.inicio.service;

import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.datasetmodelov6.service.EstadoPreparacionClienteV6Service;
import com.backend.nutri_predic.alimentacion.habito.repository.RegistroHabitoRepository;
import com.backend.nutri_predic.alimentacion.nutricion.service.ResumenNutricionalDiarioService;
import com.backend.nutri_predic.requerimientonutricional.service.ObjetivoNutricionalService;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SiguienteAccionService {
    private final ClienteRepository clientes;
    private final ObjetivoNutricionalService objetivo;
    private final EstadoPreparacionClienteV6Service preparacion;
    private final RegistroHabitoRepository habitos;
    private final ResumenNutricionalDiarioService resumen;

    public SiguienteAccionService(ClienteRepository clientes, ObjetivoNutricionalService objetivo,
            EstadoPreparacionClienteV6Service preparacion, RegistroHabitoRepository habitos, ResumenNutricionalDiarioService resumen){
        this.clientes=clientes;this.objetivo=objetivo;this.preparacion=preparacion;this.habitos=habitos;this.resumen=resumen;
    }

    @Transactional(readOnly = true)
    public String resolver(Long clienteId, LocalDate fecha){
        var cliente = clientes.findById(clienteId).orElseThrow(() -> new ResourceNotFoundException("Cliente"));
        if (cliente.getPesoKg()==null || cliente.getAlturaCm()==null || cliente.getEdad()==null || cliente.getSexo()==null)
            return "COMPLETAR_PERFIL";
        var obj = objetivo.resolver(clienteId, fecha);
        var r = resumen.resumen(clienteId, fecha);
        if (r.consumido().registrosAlimento()==0) return "REGISTRAR_CONSUMO";
        var prep = preparacion.estado(clienteId, fecha);
        if (prep!=null && prep.pendientes()!=null && !prep.pendientes().isEmpty()) return "COMPLETAR_VENTANA";
        // Si ventana completa y no hay predicción reciente
        if (prep!=null && (prep.pendientes()==null || prep.pendientes().isEmpty())) return "ANALISIS_DISPONIBLE";
        return "SIN_ACCION";
    }
}
