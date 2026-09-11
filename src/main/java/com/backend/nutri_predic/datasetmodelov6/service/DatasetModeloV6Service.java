package com.backend.nutri_predic.datasetmodelov6.service;

import com.backend.nutri_predic.datasetmodelov6.dto.CalidadDatasetModeloV6Response;
import com.backend.nutri_predic.datasetmodelov6.dto.DatasetModeloV6Row;
import com.backend.nutri_predic.perfilhabitos.entity.EvaluacionPerfilHabitos;
import com.backend.nutri_predic.perfilhabitos.repository.EvaluacionPerfilHabitosRepository;
import com.backend.nutri_predic.variablemodelov6.schema.FeatureSchemaV6Mapper;
import com.backend.nutri_predic.variablemodelov6.service.VariablesModeloV6Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatasetModeloV6Service {
    private final EvaluacionPerfilHabitosRepository evaluaciones;
    private final VariablesModeloV6Service variables;
    private final AptitudEntrenamientoV6Service aptitud;
    public DatasetModeloV6Service(EvaluacionPerfilHabitosRepository evaluaciones,
            VariablesModeloV6Service variables, AptitudEntrenamientoV6Service aptitud) {
        this.evaluaciones = evaluaciones; this.variables = variables; this.aptitud = aptitud;
    }
    @Transactional(readOnly = true)
    public List<DatasetModeloV6Row> filas() {
        return evaluaciones.findAllByOrderByIdAsc().stream().map(this::fila)
                .flatMap(Optional::stream).toList();
    }
    private Optional<DatasetModeloV6Row> fila(EvaluacionPerfilHabitos e) {
        var o = variables.construir(e.getCliente().getId(), e.getFechaCorte());
        if (!aptitud.evaluar(e, o).aptoParaEntrenamiento()) return Optional.empty();
        var r = e.getRubrica();
        return Optional.of(new DatasetModeloV6Row(new DatasetModeloV6Row.Metadata(caseId(e.getId()),
                e.getCliente().getDatasetGroupId(), e.getFechaCorte(), o.schema().schemaVersion(),
                r.getCodigo(), r.getVersion(), o.fuentePerfil()), o.schema(), e.getClasificacionReal().name()));
    }
    @Transactional(readOnly = true)
    public String csv() {
        var header = new ArrayList<String>(List.of("case_id", "cliente_group_id", "fecha_corte",
                "schema_version", "rubrica_codigo", "rubrica_version", "fuente_perfil"));
        header.addAll(FeatureSchemaV6Mapper.FEATURE_NAMES); header.add("clasificacion_real");
        var out = new StringBuilder(String.join(",", header)).append('\n');
        for (var row : filas()) {
            var m = row.metadata();
            var values = new ArrayList<String>(List.of(text(m.caseId()), text(m.clienteGroupId()),
                    text(m.fechaCorte()), text(m.schemaVersion()), text(m.rubricaCodigo()),
                    text(m.rubricaVersion()), text(m.fuentePerfil())));
            FeatureSchemaV6Mapper.FEATURE_NAMES.forEach(n -> values.add(text(row.features().features().get(n))));
            values.add(text(row.clasificacionReal()));
            out.append(String.join(",", values.stream().map(this::csvValue).toList())).append('\n');
        }
        return out.toString();
    }
    @Transactional(readOnly = true)
    public CalidadDatasetModeloV6Response calidad() {
        var distribucion = new LinkedHashMap<String, Long>();
        distribucion.put("ADECUADO", 0L); distribucion.put("MEJORABLE", 0L); distribucion.put("CRITICO", 0L);
        filas().forEach(row -> distribucion.computeIfPresent(row.clasificacionReal(), (k, v) -> v + 1));
        var noAptos = new LinkedHashMap<String, Long>();
        for (var e : evaluaciones.findAllByOrderByIdAsc()) {
            var a = aptitud.evaluar(e, variables.construir(e.getCliente().getId(), e.getFechaCorte()));
            if (!a.aptoParaEntrenamiento()) {
                if (!a.perfilHistorico()) noAptos.merge("PERFIL_HISTORICO", 1L, Long::sum);
                if (!a.xCompleta()) noAptos.merge("X_INCOMPLETAS", 1L, Long::sum);
                if (!a.groundTruthValido()) noAptos.merge("GROUND_TRUTH", 1L, Long::sum);
                if (a.smokeTecnico()) noAptos.merge("SMOKE_TECNICO", 1L, Long::sum);
            }
        }
        return new CalidadDatasetModeloV6Response(evaluaciones.count(), filas().size(),
                Map.copyOf(noAptos), Map.copyOf(distribucion));
    }
    private String text(Object value) { return value == null ? "" : value.toString(); }
    private String csvValue(String value) { return '"' + value.replace("\"", "\"\"") + '"'; }
    private String caseId(Long id) {
        try {
            var bytes = MessageDigest.getInstance("SHA-256")
                    .digest(("dataset-modelo-v6:evaluacion:" + id).getBytes(StandardCharsets.UTF_8));
            var value = new StringBuilder("case-");
            for (int i = 0; i < 12; i++) value.append(String.format("%02x", bytes[i]));
            return value.toString();
        } catch (Exception e) { throw new IllegalStateException("No se pudo anonimizar el caso V6", e); }
    }
}
