package com.backend.nutri_predic.datasetmodelov5.service;

import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.datasetmodelov5.dto.CalidadDatasetModeloV5Response;
import com.backend.nutri_predic.datasetmodelov5.dto.DatasetModeloV5Row;
import com.backend.nutri_predic.perfilhabitos.entity.EvaluacionPerfilHabitos;
import com.backend.nutri_predic.perfilhabitos.repository.EvaluacionPerfilHabitosRepository;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5Mapper;
import com.backend.nutri_predic.variablemodelov5.service.VariablesModeloV5Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatasetModeloV5Service {
    private static final List<String> HEADER = encabezado();
    private final EvaluacionPerfilHabitosRepository evaluaciones;
    private final VariablesModeloV5Service variables;
    private final FeatureSchemaV5Mapper featureMapper;
    private final AptitudEntrenamientoV5Service aptitud;

    public DatasetModeloV5Service(
            EvaluacionPerfilHabitosRepository evaluaciones,
            VariablesModeloV5Service variables,
            FeatureSchemaV5Mapper featureMapper,
            AptitudEntrenamientoV5Service aptitud) {
        this.evaluaciones = evaluaciones;
        this.variables = variables;
        this.featureMapper = featureMapper;
        this.aptitud = aptitud;
    }

    @Transactional(readOnly = true)
    public List<DatasetModeloV5Row> filas() {
        return evaluaciones.findAllByOrderByIdAsc().stream()
                .map(this::filaSiApta)
                .flatMap(java.util.Optional::stream)
                .toList();
    }

    private java.util.Optional<DatasetModeloV5Row> filaSiApta(EvaluacionPerfilHabitos evaluacion) {
        var observacion =
                variables.construir(evaluacion.getCliente().getId(), evaluacion.getFechaCorte());
        var features = featureMapper.mapear(observacion);
        if (!aptitud.evaluar(evaluacion, observacion, features.features())
                .aptoParaEntrenamiento()) {
            return java.util.Optional.empty();
        }
        var metadata =
                new DatasetModeloV5Row.Metadata(
                        caseId(evaluacion.getId()),
                        evaluacion.getCliente().getDatasetGroupId(),
                        evaluacion.getFechaCorte(),
                        observacion.schemaVersion(),
                        evaluacion.getRubrica().getCodigo(),
                        evaluacion.getRubrica().getVersion(),
                        observacion.metadata().fuentePerfil(),
                        observacion.metadata().alimentacionCompleta(),
                        observacion.metadata().suplementacionCompleta(),
                        observacion.metadata().habitosCompletos());
        return java.util.Optional.of(
                new DatasetModeloV5Row(
                        metadata, features, evaluacion.getClasificacionReal().name()));
    }

    @Transactional(readOnly = true)
    public String csv() {
        StringBuilder out = new StringBuilder(String.join(",", HEADER)).append('\n');
        for (DatasetModeloV5Row fila : filas())
            out.append(String.join(",", valores(fila))).append('\n');
        return out.toString();
    }

    @Transactional(readOnly = true)
    public CalidadDatasetModeloV5Response calidad() {
        List<DatasetModeloV5Row> filas = filas();
        Map<String, Long> distribucion = new LinkedHashMap<>();
        distribucion.put("ADECUADO", 0L);
        distribucion.put("MEJORABLE", 0L);
        distribucion.put("CRITICO", 0L);
        Map<String, Long> nulos = new LinkedHashMap<>();
        FeatureSchemaV5Mapper.FEATURE_NAMES.forEach(nombre -> nulos.put(nombre, 0L));
        long completas = 0;
        for (DatasetModeloV5Row fila : filas) {
            distribucion.computeIfPresent(fila.clasificacionReal(), (k, valor) -> valor + 1);
            boolean completa = true;
            for (String nombre : FeatureSchemaV5Mapper.FEATURE_NAMES) {
                if (fila.features().features().get(nombre) == null) {
                    nulos.computeIfPresent(nombre, (k, valor) -> valor + 1);
                    completa = false;
                }
            }
            if (completa) completas++;
        }
        Map<String, CalidadDatasetModeloV5Response.CalidadFeature> detalle = new LinkedHashMap<>();
        nulos.forEach(
                (nombre, cantidad) ->
                        detalle.put(
                                nombre,
                                new CalidadDatasetModeloV5Response.CalidadFeature(
                                        cantidad,
                                        filas.isEmpty() ? 0d : cantidad * 100d / filas.size())));
        long validas =
                evaluaciones.findAllByOrderByIdAsc().stream()
                        .filter(e -> e.getEstadoValidez() == EstadoValidezMedicion.VALIDA)
                        .count();
        return new CalidadDatasetModeloV5Response(
                validas,
                filas.size(),
                completas,
                filas.size() - completas,
                Map.copyOf(distribucion),
                Map.copyOf(detalle));
    }

    private static List<String> encabezado() {
        List<String> encabezado =
                new ArrayList<>(
                        List.of(
                                "case_id",
                                "cliente_group_id",
                                "fecha_corte",
                                "schema_version",
                                "rubrica_codigo",
                                "rubrica_version",
                                "fuente_perfil",
                                "alimentacion_completa",
                                "suplementacion_completa",
                                "habitos_completos"));
        encabezado.addAll(FeatureSchemaV5Mapper.FEATURE_NAMES);
        encabezado.add("clasificacion_real");
        return List.copyOf(encabezado);
    }

    private List<String> valores(DatasetModeloV5Row fila) {
        var m = fila.metadata();
        List<String> valores =
                new ArrayList<>(
                        List.of(
                                texto(m.caseId()),
                                texto(m.clienteGroupId()),
                                texto(m.fechaCorte()),
                                texto(m.schemaVersion()),
                                texto(m.rubricaCodigo()),
                                texto(m.rubricaVersion()),
                                texto(m.fuentePerfil()),
                                texto(m.alimentacionCompleta()),
                                texto(m.suplementacionCompleta()),
                                texto(m.habitosCompletos())));
        FeatureSchemaV5Mapper.FEATURE_NAMES.forEach(
                nombre -> valores.add(texto(fila.features().features().get(nombre))));
        valores.add(fila.clasificacionReal());
        return valores.stream().map(this::csv).toList();
    }

    private String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }

    private String csv(String valor) {
        return '"' + valor.replace("\"", "\"\"") + '"';
    }

    private String caseId(Long evaluacionId) {
        try {
            byte[] digest =
                    MessageDigest.getInstance("SHA-256")
                            .digest(
                                    ("dataset-modelo-v5:evaluacion:" + evaluacionId)
                                            .getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder("case-");
            for (int i = 0; i < 12; i++) {
                hex.append(String.format("%02x", digest[i]));
            }
            return hex.toString();
        } catch (Exception error) {
            throw new IllegalStateException("No se pudo anonimizar el caso V5", error);
        }
    }
}
