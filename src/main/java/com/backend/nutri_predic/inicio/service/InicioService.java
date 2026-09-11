package com.backend.nutri_predic.inicio.service;

import com.backend.nutri_predic.actividadfisica.service.NivelActividadFisicaService;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.datasetmodelov6.service.EstadoPreparacionClienteV6Service;
import com.backend.nutri_predic.inicio.dto.InicioResponse;
import com.backend.nutri_predic.alimentacion.nutricion.service.ResumenNutricionalDiarioService;
import com.backend.nutri_predic.plandia.repository.PlanDiarioRepository;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import com.backend.nutri_predic.requerimientonutricional.service.ObjetivoNutricionalService;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InicioService {
    private final ClienteRepository clientes;
    private final ObjetivoNutricionalService objetivo;
    private final ResumenNutricionalDiarioService resumen;
    private final EstadoPreparacionClienteV6Service preparacion;
    private final PrediccionModeloRepository predicciones;
    private final PlanDiarioRepository planes;
    private final SiguienteAccionService siguienteAccion;

    public InicioService(ClienteRepository clientes, ObjetivoNutricionalService objetivo,
            ResumenNutricionalDiarioService resumen, EstadoPreparacionClienteV6Service preparacion,
            PrediccionModeloRepository predicciones, PlanDiarioRepository planes, SiguienteAccionService siguienteAccion) {
        this.clientes = clientes; this.objetivo = objetivo; this.resumen = resumen; this.preparacion = preparacion;
        this.predicciones = predicciones; this.planes = planes; this.siguienteAccion = siguienteAccion;
    }

    @Transactional(readOnly = true)
    public InicioResponse inicio(Long clienteId, LocalDate fecha) {
        if (!clientes.existsById(clienteId)) throw new ResourceNotFoundException("Cliente");
        var obj = objetivo.resolver(clienteId, fecha);
        var res = resumen.resumen(clienteId, fecha);
        var prep = preparacion.estado(clienteId, fecha);
        var ultima = predicciones.findFirstByClienteIdOrderByFechaPrediccionDesc(clienteId).orElse(null);
        var plan = planes.findByClienteIdAndFechaObjetivoBetweenOrderByFechaObjetivoAsc(clienteId, fecha.plusDays(1), fecha.plusDays(8)).stream().findFirst().orElse(null);
        var accion = siguienteAccion.resolver(clienteId, fecha);
        return new InicioResponse(obj, res, prep, ultima, plan, null, accion);
    }
}
