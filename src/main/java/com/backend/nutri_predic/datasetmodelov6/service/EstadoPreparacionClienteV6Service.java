package com.backend.nutri_predic.datasetmodelov6.service;

import com.backend.nutri_predic.datasetmodelov6.dto.EstadoPreparacionClienteV6Response;
import com.backend.nutri_predic.perfilhabitos.repository.EvaluacionPerfilHabitosRepository;
import com.backend.nutri_predic.variablemodelov6.schema.FeatureSchemaV6Mapper;
import com.backend.nutri_predic.variablemodelov6.service.VariablesModeloV6Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstadoPreparacionClienteV6Service {
    private final VariablesModeloV6Service variables;
    private final AptitudEntrenamientoV6Service aptitud;
    private final EvaluacionPerfilHabitosRepository evaluaciones;
    public EstadoPreparacionClienteV6Service(VariablesModeloV6Service variables,
            AptitudEntrenamientoV6Service aptitud, EvaluacionPerfilHabitosRepository evaluaciones) {
        this.variables = variables; this.aptitud = aptitud; this.evaluaciones = evaluaciones;
    }
    @Transactional(readOnly = true)
    public EstadoPreparacionClienteV6Response estado(Long id, LocalDate corte) {
        var observacion = variables.construir(id, corte);
        var relacionados = evaluaciones.findAllByOrderByIdAsc().stream()
                .filter(e -> id.equals(e.getCliente().getId()) && corte.equals(e.getFechaCorte())).toList();
        var resultado = relacionados.stream().map(e -> aptitud.evaluar(e, observacion))
                .filter(AptitudEntrenamientoV6Service.Resultado::groundTruthValido).findFirst()
                .orElseGet(() -> relacionados.isEmpty() ? aptitud.evaluar(null, observacion)
                        : aptitud.evaluar(relacionados.getLast(), observacion));
        var motivos = new LinkedHashMap<String, String>();
        observacion.schema().features().forEach((nombre, valor) -> {
            if (valor == null) motivos.put(nombre, "PERFIL_ESENCIAL_FALTANTE");
        });
        var pendientes = new ArrayList<String>();
        if (!observacion.perfilHistorico()) pendientes.add("PERFIL_HISTORICO_APLICABLE_REQUERIDO");
        if (!resultado.groundTruthValido()) pendientes.add("GROUND_TRUTH_VALIDO_REQUERIDO");
        return new EstadoPreparacionClienteV6Response(id, corte, resultado.xDisponibles(),
                FeatureSchemaV6Mapper.FEATURE_NAMES.size(), observacion.perfilHistorico(),
                resultado.groundTruthValido(), resultado.smokeTecnico(),
                resultado.aptoParaEntrenamiento(), List.copyOf(pendientes), Map.copyOf(motivos));
    }
}
