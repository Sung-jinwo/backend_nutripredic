package com.backend.nutri_predic.datasetmodelov5.service;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.perfilhabitos.entity.ClasificacionPerfilHabitos;
import com.backend.nutri_predic.perfilhabitos.entity.EstadoRubricaPerfilHabitos;
import com.backend.nutri_predic.perfilhabitos.entity.EvaluacionPerfilHabitos;
import com.backend.nutri_predic.perfilhabitos.entity.RubricaPerfilHabitos;
import com.backend.nutri_predic.variablemodelov5.dto.VariablesModeloV5Response;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5Mapper;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AptitudEntrenamientoV5Service {

    public Resultado evaluar(
            EvaluacionPerfilHabitos evaluacion,
            VariablesModeloV5Response observacion,
            Map<String, Object> x) {
        boolean schemaValido =
                observacion != null
                        && FeatureSchemaV5Mapper.SCHEMA_VERSION.equals(observacion.schemaVersion());
        boolean fechaCorteValida =
                evaluacion != null
                        && evaluacion.getFechaCorte() != null
                        && !evaluacion.getFechaCorte().isAfter(LocalDate.now())
                        && observacion != null
                        && evaluacion.getFechaCorte().equals(observacion.fechaCorte());
        boolean perfilCompleto =
                observacion != null
                        && observacion.metadata() != null
                        && observacion.metadata().fuentePerfil() != null
                        && !observacion.metadata().fuentePerfil().isBlank()
                        && !"FALTANTE".equals(observacion.metadata().fuentePerfil())
                        && x.get("edad") != null
                        && x.get("peso_kg") != null
                        && x.get("altura_cm") != null
                        && x.get("tipo_objetivo_fisico") != null;
        boolean alimentacionCompleta =
                observacion != null
                        && observacion.metadata() != null
                        && observacion.metadata().alimentacionCompleta();
        boolean suplementacionCompleta =
                observacion != null
                        && observacion.metadata() != null
                        && observacion.metadata().suplementacionCompleta();
        boolean habitosCompletos =
                observacion != null
                        && observacion.metadata() != null
                        && observacion.metadata().habitosCompletos();
        int xDisponibles =
                (int)
                        FeatureSchemaV5Mapper.FEATURE_NAMES.stream()
                                .filter(nombre -> x.containsKey(nombre) && x.get(nombre) != null)
                                .count();
        boolean xCompleta =
                x.size() == FeatureSchemaV5Mapper.FEATURE_NAMES.size()
                        && xDisponibles == FeatureSchemaV5Mapper.FEATURE_NAMES.size()
                        && x.keySet().containsAll(FeatureSchemaV5Mapper.FEATURE_NAMES);
        boolean groundTruthValido = groundTruthValido(evaluacion);
        boolean smokeTecnico = esSmokeTecnico(evaluacion);
        boolean clasificacionValida =
                evaluacion != null
                        && evaluacion.getClasificacionReal() != null
                        && EnumSet.allOf(ClasificacionPerfilHabitos.class)
                                .contains(evaluacion.getClasificacionReal());
        boolean apto =
                schemaValido
                        && fechaCorteValida
                        && perfilCompleto
                        && alimentacionCompleta
                        && suplementacionCompleta
                        && habitosCompletos
                        && xCompleta
                        && groundTruthValido
                        && clasificacionValida
                        && !smokeTecnico;
        return new Resultado(
                schemaValido,
                fechaCorteValida,
                perfilCompleto,
                alimentacionCompleta,
                suplementacionCompleta,
                habitosCompletos,
                xDisponibles,
                xCompleta,
                groundTruthValido,
                smokeTecnico,
                clasificacionValida,
                apto);
    }

    public boolean groundTruthValido(EvaluacionPerfilHabitos evaluacion) {
        return evaluacion != null
                && evaluacion.getEstadoValidez() == EstadoValidezMedicion.VALIDA
                && rubricaValida(evaluacion);
    }

    public boolean rubricaValida(EvaluacionPerfilHabitos evaluacion) {
        return evaluacion != null
                && evaluacion.getRubrica() != null
                && evaluacion.getRubrica().getEstado() == EstadoRubricaPerfilHabitos.ACTIVA
                && rubricaValidada(evaluacion.getRubrica())
                && evaluacion.getRubrica().getValidadoEn() != null
                && vigenteParaFechaCorte(evaluacion);
    }

    private boolean rubricaValidada(RubricaPerfilHabitos rubrica) {
        String tipo = rubrica.getTipoValidacion();
        if ("VALIDACION_TECNICO_DOCUMENTAL".equals(tipo)) {
            return rubrica.getFuenteValidacion() != null
                    && !rubrica.getFuenteValidacion().isBlank()
                    && rubrica.getVersionFuente() != null
                    && rubrica.getValidadoEn() != null;
        }
        return rubrica.getValidadoPor() != null
                && !rubrica.getValidadoPor().isBlank()
                && rubrica.getValidadoEn() != null;
    }

    public boolean esSmokeTecnico(EvaluacionPerfilHabitos evaluacion) {
        if (evaluacion == null) {
            return false;
        }
        if (esClienteTecnico(evaluacion.getCliente())) {
            return true;
        }
        String observacion = texto(evaluacion.getObservacion());
        String codigo =
                evaluacion.getRubrica() == null ? "" : texto(evaluacion.getRubrica().getCodigo());
        return observacion.contains("SMOKE TECNICO DATASET V5")
                || codigo.contains("SMOKE_TECNICO_DATASET_V5");
    }

    public boolean esClienteTecnico(Cliente cliente) {
        if (cliente == null) {
            return false;
        }
        String email = cliente.getUsuario() == null ? "" : texto(cliente.getUsuario().getEmail());
        String nombre = cliente.getUsuario() == null ? "" : texto(cliente.getUsuario().getNombre());
        String objetivo = texto(cliente.getObjetivoFisico());
        return email.endsWith("@E2E.NUTRIPREDIC.LOCAL")
                || email.equals("SMOKE-TECNICO-PCC-IA@DEV.NUTRIPREDIC.LOCAL")
                || nombre.contains("DATOS DE PRUEBA E2E ML")
                || nombre.contains("SMOKE TECNICO PCC-IA")
                || objetivo.contains("DATOS DE PRUEBA E2E ML")
                || objetivo.contains("NO_USAR_ENTRENAMIENTO");
    }

    private boolean vigenteParaFechaCorte(EvaluacionPerfilHabitos evaluacion) {
        if (evaluacion.getFechaCorte() == null
                || evaluacion.getRubrica().getVigenteDesde() == null) {
            return false;
        }
        ZoneId zona = ZoneId.systemDefault();
        LocalDate vigenteDesde =
                evaluacion.getRubrica().getVigenteDesde().atZone(zona).toLocalDate();
        LocalDate vigenteHasta =
                evaluacion.getRubrica().getVigenteHasta() == null
                        ? null
                        : evaluacion.getRubrica().getVigenteHasta().atZone(zona).toLocalDate();
        return !evaluacion.getFechaCorte().isBefore(vigenteDesde)
                && (vigenteHasta == null || !evaluacion.getFechaCorte().isAfter(vigenteHasta));
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.toUpperCase(Locale.ROOT);
    }

    public record Resultado(
            boolean schemaValido,
            boolean fechaCorteValida,
            boolean perfilCompleto,
            boolean alimentacionCompleta,
            boolean suplementacionCompleta,
            boolean habitosCompletos,
            int xDisponibles,
            boolean xCompleta,
            boolean groundTruthValido,
            boolean smokeTecnico,
            boolean clasificacionValida,
            boolean aptoParaEntrenamiento) {}
}
