package com.backend.nutri_predic.historial.service;

import com.backend.nutri_predic.alimentacion.repository.RegistroAlimentoRepository;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.alimentacion.nutricion.service.ResumenNutricionalDiarioService;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import com.backend.nutri_predic.conocimiento.evaluacion.repository.ResultadoTestRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistorialIntegradoService {
    private final ClienteRepository clientes;
    private final ResumenNutricionalDiarioService resumen;
    private final PrediccionModeloRepository predicciones;
    private final ResultadoTestRepository tests;
    private final RegistroAlimentoRepository alimentos;

    public HistorialIntegradoService(ClienteRepository clientes, ResumenNutricionalDiarioService resumen,
            PrediccionModeloRepository predicciones, ResultadoTestRepository tests, RegistroAlimentoRepository alimentos){
        this.clientes=clientes;this.resumen=resumen;this.predicciones=predicciones;this.tests=tests;this.alimentos=alimentos;
    }

    @Transactional(readOnly = true)
    public Map<String,Object> historial(Long clienteId, LocalDate desde, LocalDate hasta){
        if (!clientes.existsById(clienteId)) throw new ResourceNotFoundException("Cliente");
        List<Map<String,Object>> dias = new ArrayList<>();
        for (LocalDate d = desde; !d.isAfter(hasta); d = d.plusDays(1)){
            var r = resumen.resumen(clienteId, d);
            dias.add(Map.of("fecha", d.toString(), "resumen", r));
        }
        var todasPreds = predicciones.findByClienteIdOrderByFechaPrediccionDesc(clienteId);
        var preds = todasPreds.stream().filter(p -> p.getFechaCorte()!=null && !p.getFechaCorte().isBefore(desde) && !p.getFechaCorte().isAfter(hasta)).toList();
        var todosTests = tests.findByClienteIdOrderByFechaDesc(clienteId);
        var zona = java.time.ZoneId.of("America/Lima");
        var desdeInst = desde.atStartOfDay(zona).toInstant();
        var hastaInst = hasta.plusDays(1).atStartOfDay(zona).toInstant();
        var tsts = todosTests.stream().filter(t -> t.getFecha()!=null && !t.getFecha().isBefore(desdeInst) && t.getFecha().isBefore(hastaInst)).toList();
        return Map.of("clienteId", clienteId, "desde", desde.toString(), "hasta", hasta.toString(),
                "dias", dias, "predicciones", preds, "tests", tsts);
    }
}
