package com.backend.nutri_predic.variablemodelov6.service;

import com.backend.nutri_predic.cliente.repository.HistorialPerfilClienteRepository;
import com.backend.nutri_predic.alimentacion.nutricion.service.ResumenNutricionalDiarioService;
import com.backend.nutri_predic.variablemodelov6.schema.FeatureSchemaV6;
import com.backend.nutri_predic.variablemodelov6.schema.FeatureSchemaV6Mapper;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VariablesModeloV6Service {
    private final FeatureSchemaV6Mapper mapper;
    private final HistorialPerfilClienteRepository perfiles;
    private final ResumenNutricionalDiarioService resumenes;

    public VariablesModeloV6Service(
            FeatureSchemaV6Mapper mapper, HistorialPerfilClienteRepository perfiles,
            ResumenNutricionalDiarioService resumenes) {
        this.mapper = mapper;
        this.perfiles = perfiles;
        this.resumenes = resumenes;
    }

    @Transactional(readOnly = true)
    public Observacion construir(Long clienteId, LocalDate fechaCorte) {
        var perfil = perfiles
                .findFirstByClienteIdAndFechaDesdeLessThanEqualOrderByFechaDesdeDescCreadoEnDescIdDesc(
                        clienteId, fechaCorte)
                .filter(p -> !p.getCreadoEn().isAfter(
                        fechaCorte.plusDays(1).atStartOfDay(ZoneId.of("America/Lima")).toInstant()))
                .orElse(null);
        var valores = new LinkedHashMap<String, Object>();
        valores.put("edad", perfil == null ? null : perfil.getEdad());
        valores.put("peso_kg", perfil == null ? null : perfil.getPesoKg());
        valores.put("altura_cm", perfil == null ? null : perfil.getAlturaCm());
        valores.put("sexo_biologico", perfil == null || perfil.getSexo() == null ? null : perfil.getSexo().name());
        valores.put("tipo_objetivo_fisico", perfil == null || perfil.getTipoObjetivoFisico() == null
                ? null : perfil.getTipoObjetivoFisico().name());
        valores.put("objetivo_energetico", perfil == null || perfil.getObjetivoEnergetico() == null
                ? null : perfil.getObjetivoEnergetico().name());
        valores.put("dias_entrenamiento_semana", perfil == null ? null : perfil.getDiasEntrenamientoSemana());
        valores.put("duracion_promedio_sesion_minutos", perfil == null ? null : perfil.getDuracionPromedioSesionMinutos());
        var resumenAnterior = resumenes.resumen(clienteId, fechaCorte.minusDays(1));
        boolean registroAnteriorCompleto = resumenAnterior.consumido().registrosAlimento() > 0
                && resumenAnterior.consumido().total().calculable()
                && resumenAnterior.agua().declarado()
                && "DISPONIBLE".equals(resumenAnterior.objetivo().estado());
        var total = resumenAnterior.consumido().total();
        valores.put("consumo_kcal_dia_anterior", registroAnteriorCompleto ? total.kcal() : null);
        valores.put("consumo_proteina_g_dia_anterior", registroAnteriorCompleto ? total.proteinaG() : null);
        valores.put("consumo_carbohidratos_g_dia_anterior", registroAnteriorCompleto ? total.carbohidratosG() : null);
        valores.put("consumo_grasas_g_dia_anterior", registroAnteriorCompleto ? total.grasasG() : null);
        valores.put("consumo_agua_ml_dia_anterior", registroAnteriorCompleto ? resumenAnterior.agua().consumidoMl() : null);
        return new Observacion(mapper.mapear(valores), "HISTORIAL_PERFIL_CLIENTE", perfil != null,
                registroAnteriorCompleto, fechaCorte.minusDays(1));
    }

    public record Observacion(
            FeatureSchemaV6 schema,
            String fuentePerfil,
            boolean perfilHistorico,
            boolean registroAnteriorCompleto,
            LocalDate fechaRegistroEvaluado) {}
}
