package com.backend.nutri_predic.datasetmodelov6.service;

import com.backend.nutri_predic.datasetmodelov5.service.AptitudEntrenamientoV5Service;
import com.backend.nutri_predic.perfilhabitos.entity.EvaluacionPerfilHabitos;
import com.backend.nutri_predic.variablemodelov6.schema.FeatureSchemaV6Mapper;
import com.backend.nutri_predic.variablemodelov6.service.VariablesModeloV6Service.Observacion;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class AptitudEntrenamientoV6Service {
    private final AptitudEntrenamientoV5Service base;
    public AptitudEntrenamientoV6Service(AptitudEntrenamientoV5Service base) { this.base = base; }
    public Resultado evaluar(EvaluacionPerfilHabitos e, Observacion o) {
        int x = (int) o.schema().features().values().stream().filter(java.util.Objects::nonNull).count();
        boolean completa = x == FeatureSchemaV6Mapper.FEATURE_NAMES.size();
        boolean fecha = e != null && e.getFechaCorte() != null && !e.getFechaCorte().isAfter(LocalDate.now())
                && FeatureSchemaV6Mapper.SCHEMA_VERSION.equals(o.schema().schemaVersion());
        boolean smoke = base.esSmokeTecnico(e);
        boolean groundTruth = base.groundTruthValido(e) || (smoke && groundTruthTecnicoValido(e));
        boolean apto = fecha && o.perfilHistorico() && completa && groundTruth && !smoke;
        return new Resultado(x, completa, o.perfilHistorico(), groundTruth, smoke, fecha, apto);
    }
    private boolean groundTruthTecnicoValido(EvaluacionPerfilHabitos e) {
        return e != null && e.getEstadoValidez() == com.backend.nutri_predic.common.enums.EstadoValidezMedicion.VALIDA
                && e.getRubrica() != null && "RUBRICA_PERFIL_HABITOS_V1".equals(e.getRubrica().getCodigo())
                && java.math.BigDecimal.valueOf(90, 2).compareTo(e.getRubrica().getVersion()) == 0
                && e.getRubrica().getEstado() == com.backend.nutri_predic.perfilhabitos.entity.EstadoRubricaPerfilHabitos.BORRADOR
                && e.getObservacion() != null && e.getObservacion().contains("SYNTHETIC_TECHNICAL");
    }
    public record Resultado(int xDisponibles, boolean xCompleta, boolean perfilHistorico,
            boolean groundTruthValido, boolean smokeTecnico, boolean fechaCorteValida,
            boolean aptoParaEntrenamiento) {}
}
