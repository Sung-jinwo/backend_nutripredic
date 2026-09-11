package com.backend.nutri_predic.datasetmodelov5.service;

import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.datasetmodelov5.dto.PreparacionDatasetModeloV5Response;
import com.backend.nutri_predic.perfilhabitos.repository.EvaluacionPerfilHabitosRepository;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5Mapper;
import com.backend.nutri_predic.variablemodelov5.service.VariablesModeloV5Service;
import java.util.*;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreparacionDatasetModeloV5Service {
    private final ClienteRepository clientes;
    private final EvaluacionPerfilHabitosRepository evaluaciones;
    private final VariablesModeloV5Service variables;
    private final FeatureSchemaV5Mapper mapper;
    private final AptitudEntrenamientoV5Service aptitud;

    public PreparacionDatasetModeloV5Service(
            ClienteRepository c,
            EvaluacionPerfilHabitosRepository e,
            VariablesModeloV5Service v,
            FeatureSchemaV5Mapper m,
            AptitudEntrenamientoV5Service a) {
        clientes = c;
        evaluaciones = e;
        variables = v;
        mapper = m;
        aptitud = a;
    }

    @Transactional(readOnly = true)
    public PreparacionDatasetModeloV5Response auditar() {
        var filas = evaluaciones.findAllByOrderByIdAsc().stream().map(this::fila).toList();
        return new PreparacionDatasetModeloV5Response(resumen(filas), filas);
    }

    private PreparacionDatasetModeloV5Response.Fila fila(
            com.backend.nutri_predic.perfilhabitos.entity.EvaluacionPerfilHabitos e) {
        var o = variables.construir(e.getCliente().getId(), e.getFechaCorte());
        Map<String, Object> x = mapper.mapear(o).features();
        var resultado = aptitud.evaluar(e, o, x);
        Map<String, String> motivos = new LinkedHashMap<>();
        x.forEach(
                (n, v) -> {
                    if (v == null) motivos.put(n, motivo(n, o));
                });
        return new PreparacionDatasetModeloV5Response.Fila(
                e.getCliente().getId(),
                e.getFechaCorte(),
                e.getId(),
                resultado.xDisponibles(),
                motivos.size(),
                resultado.xDisponibles() * 100d / FeatureSchemaV5Mapper.FEATURE_NAMES.size(),
                resultado.alimentacionCompleta(),
                resultado.suplementacionCompleta(),
                resultado.habitosCompletos(),
                resultado.perfilCompleto(),
                resultado.groundTruthValido(),
                resultado.smokeTecnico(),
                resultado.aptoParaEntrenamiento(),
                Map.copyOf(motivos));
    }

    private String motivo(
            String f, com.backend.nutri_predic.variablemodelov5.dto.VariablesModeloV5Response o) {
        if (List.of("edad", "peso_kg", "altura_cm").contains(f)) return "PERFIL_FALTANTE";
        if (f.equals("tipo_objetivo_fisico")) return "OBJETIVO_FISICO_FALTANTE";
        if (f.startsWith("promedio_kcal_")
                || f.startsWith("promedio_proteina_g_")
                || f.startsWith("promedio_carbohidratos_g_")
                || f.startsWith("promedio_grasas_g_")
                || f.startsWith("promedio_fibra_")
                || f.startsWith("promedio_azucar_")
                || f.startsWith("promedio_sodio_"))
            return o.metadata().alimentacionCompleta()
                    ? "NUTRIENTE_NO_CALCULABLE"
                    : "VENTANA_ALIMENTACION_INCOMPLETA";
        if (f.contains("suplementaria")
                || f.contains("creatina")
                || f.contains("cafeina")
                || f.contains("suplementarios"))
            return o.metadata().suplementacionCompleta()
                    ? "COMPONENTE_SUPLEMENTO_DESCONOCIDO"
                    : "VENTANA_SUPLEMENTACION_INCOMPLETA";
        return o.metadata().habitosCompletos()
                ? "CAMPO_HABITO_FALTANTE"
                : "VENTANA_HABITOS_INCOMPLETA";
    }

    private PreparacionDatasetModeloV5Response.Resumen resumen(
            List<PreparacionDatasetModeloV5Response.Fila> f) {
        return new PreparacionDatasetModeloV5Response.Resumen(
                clientes.count(),
                f.size(),
                distintos(f, PreparacionDatasetModeloV5Response.Fila::perfilDisponible),
                distintos(f, PreparacionDatasetModeloV5Response.Fila::alimentacionCompleta),
                distintos(f, PreparacionDatasetModeloV5Response.Fila::suplementacionCompleta),
                distintos(f, PreparacionDatasetModeloV5Response.Fila::habitosCompletos),
                contar(f, x -> x.cantidadXNoNull() == FeatureSchemaV5Mapper.FEATURE_NAMES.size()),
                contar(f, PreparacionDatasetModeloV5Response.Fila::groundTruthValido),
                contar(f, PreparacionDatasetModeloV5Response.Fila::aptoParaEntrenamientoCompleto));
    }

    private long distintos(
            List<PreparacionDatasetModeloV5Response.Fila> f,
            Predicate<PreparacionDatasetModeloV5Response.Fila> p) {
        return f.stream()
                .filter(p)
                .map(PreparacionDatasetModeloV5Response.Fila::clienteId)
                .distinct()
                .count();
    }

    private long contar(
            List<PreparacionDatasetModeloV5Response.Fila> f,
            Predicate<PreparacionDatasetModeloV5Response.Fila> p) {
        return f.stream().filter(p).count();
    }
}
