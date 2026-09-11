package com.backend.nutri_predic.datasetmodelov6.service;

import com.backend.nutri_predic.datasetmodelov6.dto.*;
import com.backend.nutri_predic.perfilhabitos.entity.EvaluacionPerfilHabitos;
import com.backend.nutri_predic.perfilhabitos.repository.EvaluacionPerfilHabitosRepository;
import com.backend.nutri_predic.variablemodelov6.schema.FeatureSchemaV6Mapper;
import com.backend.nutri_predic.variablemodelov6.service.VariablesModeloV6Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatasetModeloV6TechnicalService {
    private final EvaluacionPerfilHabitosRepository evaluaciones;
    private final VariablesModeloV6Service variables;
    private final AptitudEntrenamientoV6Service aptitud;

    public DatasetModeloV6TechnicalService(EvaluacionPerfilHabitosRepository e, VariablesModeloV6Service v, AptitudEntrenamientoV6Service a) {
        evaluaciones = e;
        variables = v;
        aptitud = a;
    }

    @Transactional(readOnly = true)
    public List<DatasetModeloV6Row> filasTecnicas() {
        return evaluaciones.findAllByOrderByIdAsc().stream()
                .map(this::filaTecnica)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<DatasetModeloV6Row> filaTecnica(EvaluacionPerfilHabitos e) {
        var o = variables.construir(e.getCliente().getId(), e.getFechaCorte());
        var aptoCheck = aptitud.evaluar(e, o);
        if (!aptoCheck.fechaCorteValida()) return Optional.empty();
        if (!o.perfilHistorico()) return Optional.empty();
        if (!aptoCheck.xCompleta()) return Optional.empty();
        if (!aptoCheck.groundTruthValido()) return Optional.empty();
        if (!aptoCheck.smokeTecnico()) return Optional.empty();
        var r = e.getRubrica();
        return Optional.of(new DatasetModeloV6Row(
                new DatasetModeloV6Row.Metadata(
                        caseId(e.getId()),
                        e.getCliente().getDatasetGroupId(),
                        e.getFechaCorte(),
                        o.schema().schemaVersion(),
                        r.getCodigo(),
                        r.getVersion(),
                        o.fuentePerfil()),
                o.schema(),
                e.getClasificacionReal().name()));
    }

    @Transactional(readOnly = true)
    public String csv() {
        var h = new ArrayList<String>(List.of("case_id", "cliente_group_id", "fecha_corte", "schema_version", "rubrica_codigo", "rubrica_version", "fuente_perfil"));
        h.addAll(FeatureSchemaV6Mapper.FEATURE_NAMES);
        h.add("clasificacion_real");
        h.add("data_type");
        var out = new StringBuilder(String.join(",", h)).append('\n');
        for (var r : filasTecnicas()) {
            var m = r.metadata();
            var v = new ArrayList<String>(List.of(t(m.caseId()), tObj(m.clienteGroupId()), tObj(m.fechaCorte()), t(m.schemaVersion()), t(m.rubricaCodigo()), tObj(m.rubricaVersion()), t(m.fuentePerfil())));
            FeatureSchemaV6Mapper.FEATURE_NAMES.forEach(n -> v.add(tObj(r.features().features().get(n))));
            v.add(t(r.clasificacionReal()));
            v.add("SYNTHETIC_TECHNICAL");
            out.append(String.join(",", v.stream().map(this::csv).toList())).append('\n');
        }
        return out.toString();
    }

    private String t(String s) {
        return s == null ? "" : s;
    }

    private String tObj(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private String caseId(Long id) {
        return "case-" + id;
    }

    private String csv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
