package com.backend.nutri_predic.datasetmodelov5.service;

import com.backend.nutri_predic.datasetmodelov5.dto.EstadoPreparacionClienteV5Response;
import com.backend.nutri_predic.observaciondiaria.entity.DominioObservacionDiaria;
import com.backend.nutri_predic.observaciondiaria.service.CompletitudVentanaV5Service;
import com.backend.nutri_predic.perfilhabitos.repository.EvaluacionPerfilHabitosRepository;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5Mapper;
import com.backend.nutri_predic.variablemodelov5.service.VariablesModeloV5Service;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstadoPreparacionClienteV5Service {
    private final VariablesModeloV5Service variables;
    private final FeatureSchemaV5Mapper mapper;
    private final CompletitudVentanaV5Service completitud;
    private final EvaluacionPerfilHabitosRepository evaluaciones;
    private final AptitudEntrenamientoV5Service aptitud;

    public EstadoPreparacionClienteV5Service(
            VariablesModeloV5Service v,
            FeatureSchemaV5Mapper m,
            CompletitudVentanaV5Service c,
            EvaluacionPerfilHabitosRepository e,
            AptitudEntrenamientoV5Service a) {
        variables = v;
        mapper = m;
        completitud = c;
        evaluaciones = e;
        aptitud = a;
    }

    @Transactional(readOnly = true)
    public EstadoPreparacionClienteV5Response estado(Long id, LocalDate corte) {
        var o = variables.construir(id, corte);
        var x = mapper.mapear(o).features();
        var a = completitud.verificar(id, corte, DominioObservacionDiaria.ALIMENTACION);
        var s = completitud.verificar(id, corte, DominioObservacionDiaria.SUPLEMENTACION);
        var h = completitud.verificar(id, corte, DominioObservacionDiaria.HABITOS);
        Map<String, String> motivos = new LinkedHashMap<>();
        x.forEach(
                (n, v) -> {
                    if (v == null) motivos.put(n, motivo(n, o));
                });
        var contexto = aptitud.evaluar(null, o, x);
        var rel =
                evaluaciones.findAllByOrderByIdAsc().stream()
                        .filter(
                                e ->
                                        e.getCliente().getId().equals(id)
                                                && e.getFechaCorte().equals(corte))
                        .toList();
        var resultados = rel.stream().map(e -> aptitud.evaluar(e, o, x)).toList();
        boolean smoke = resultados.stream().anyMatch(xResultado -> xResultado.smokeTecnico());
        boolean y = resultados.stream().anyMatch(xResultado -> xResultado.groundTruthValido());
        boolean apto =
                resultados.stream().anyMatch(xResultado -> xResultado.aptoParaEntrenamiento());
        List<String> pendientes = new ArrayList<>();
        if (x.get("tipo_objetivo_fisico") == null) pendientes.add("REGISTRAR_TIPO_OBJETIVO_FISICO");
        pendientesDias(pendientes, "COMPLETAR_ALIMENTACION", a);
        pendientesDias(pendientes, "COMPLETAR_SUPLEMENTACION", s);
        pendientesDias(pendientes, "COMPLETAR_HABITOS", h);
        if (!a.ventanaCompleta() || motivos.containsValue("NUTRIENTE_NO_CALCULABLE"))
            pendientes.add("COMPLETAR_COMPOSICION_ALIMENTO");
        if (!s.ventanaCompleta() || motivos.containsValue("COMPONENTE_SUPLEMENTO_DESCONOCIDO"))
            pendientes.add("COMPLETAR_COMPOSICION_SUPLEMENTO");
        if (!y) pendientes.add("REGISTRAR_GROUND_TRUTH");
        int nut =
                contar(
                        x,
                        List.of(
                                "promedio_kcal_7d",
                                "promedio_proteina_g_7d",
                                "promedio_carbohidratos_g_7d",
                                "promedio_grasas_g_7d",
                                "promedio_fibra_g_7d",
                                "promedio_azucar_g_7d",
                                "promedio_sodio_mg_7d"));
        int sup =
                contar(
                        x,
                        List.of(
                                "promedio_proteina_suplementaria_g_7d",
                                "promedio_creatina_g_7d",
                                "promedio_cafeina_mg_7d",
                                "promedio_carbohidratos_suplementarios_g_7d",
                                "promedio_grasas_suplementarias_g_7d"));
        int hab =
                contar(
                        x,
                        List.of(
                                "promedio_cantidad_comidas_7d",
                                "promedio_consumo_agua_7d",
                                "proporcion_desayuno_7d",
                                "proporcion_snacks_7d",
                                "promedio_comidas_cocinadas_7d"));
        return new EstadoPreparacionClienteV5Response(
                id,
                corte,
                contexto.perfilCompleto(),
                a.diasCompletos(),
                s.diasCompletos(),
                h.diasCompletos(),
                nut,
                sup,
                hab,
                y,
                smoke,
                contexto.xDisponibles(),
                apto,
                List.copyOf(pendientes),
                Map.copyOf(motivos));
    }

    private int contar(Map<String, Object> x, List<String> n) {
        return (int) n.stream().filter(k -> x.get(k) != null).count();
    }

    private void pendientesDias(
            List<String> p,
            String pref,
            com.backend.nutri_predic.observaciondiaria.dto.CompletitudVentanaV5Response r) {
        if (!r.ventanaCompleta()) p.add(pref + "_VENTANA_" + r.fechaDesde() + "_" + r.fechaCorte());
    }

    private String motivo(
            String n, com.backend.nutri_predic.variablemodelov5.dto.VariablesModeloV5Response o) {
        if (List.of("edad", "peso_kg", "altura_cm").contains(n)) return "PERFIL_FALTANTE";
        if (n.equals("tipo_objetivo_fisico")) return "OBJETIVO_FISICO_FALTANTE";
        if (n.contains("suplement") || n.contains("creatina") || n.contains("cafeina"))
            return o.metadata().suplementacionCompleta()
                    ? "COMPONENTE_SUPLEMENTO_DESCONOCIDO"
                    : "VENTANA_SUPLEMENTACION_INCOMPLETA";
        if (n.startsWith("promedio_")
                && !n.contains("cantidad")
                && !n.contains("consumo_agua")
                && !n.contains("comidas_cocinadas"))
            return o.metadata().alimentacionCompleta()
                    ? "NUTRIENTE_NO_CALCULABLE"
                    : "VENTANA_ALIMENTACION_INCOMPLETA";
        return o.metadata().habitosCompletos()
                ? "CAMPO_HABITO_FALTANTE"
                : "VENTANA_HABITOS_INCOMPLETA";
    }
}
