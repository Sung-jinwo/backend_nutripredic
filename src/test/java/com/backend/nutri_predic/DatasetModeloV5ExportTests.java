package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.datasetmodelov5.service.AptitudEntrenamientoV5Service;
import com.backend.nutri_predic.datasetmodelov5.service.DatasetModeloV5Service;
import com.backend.nutri_predic.datasetmodelov5.service.EstadoPreparacionClienteV5Service;
import com.backend.nutri_predic.datasetmodelov5.service.PreparacionDatasetModeloV5Service;
import com.backend.nutri_predic.observaciondiaria.dto.CompletitudVentanaV5Response;
import com.backend.nutri_predic.observaciondiaria.entity.DominioObservacionDiaria;
import com.backend.nutri_predic.observaciondiaria.service.CompletitudVentanaV5Service;
import com.backend.nutri_predic.perfilhabitos.entity.ClasificacionPerfilHabitos;
import com.backend.nutri_predic.perfilhabitos.entity.EstadoRubricaPerfilHabitos;
import com.backend.nutri_predic.perfilhabitos.entity.EvaluacionPerfilHabitos;
import com.backend.nutri_predic.perfilhabitos.entity.RubricaPerfilHabitos;
import com.backend.nutri_predic.perfilhabitos.repository.EvaluacionPerfilHabitosRepository;
import com.backend.nutri_predic.variablemodelov5.dto.VariablesModeloV5Response;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5Mapper;
import com.backend.nutri_predic.variablemodelov5.service.VariablesModeloV5Service;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DatasetModeloV5ExportTests {

    @Test
    void exportaSoloFilaAptaYPreparacionUsaElMismoConteo() {
        Fixture fixture = fixture();

        var filas = fixture.dataset.filas();
        var preparacion = fixture.preparacion.auditar();

        assertThat(filas).singleElement();
        assertThat(filas.getFirst().metadata().caseId()).startsWith("case-");
        assertThat(filas.getFirst().metadata().rubricaCodigo()).isEqualTo("RUBRICA_REAL_V5");
        assertThat(filas.getFirst().metadata().rubricaVersion()).isEqualTo(java.math.BigDecimal.valueOf(3));
        assertThat(filas.getFirst().features().features())
                .hasSize(21)
                .doesNotContainKeys(
                        "cliente_id",
                        "usuario_id",
                        "grupo_control",
                        "grupo_experimental",
                        "pcc",
                        "pcs",
                        "tpp",
                        "prediccion_previa");
        assertThat(preparacion.resumen().filasPotencialmenteEntrenables()).isEqualTo(filas.size());
        assertThat(
                        fixture.estado
                                .estado(5L, fixture.evaluacionesCreadas.get(4).getFechaCorte())
                                .aptoParaEntrenamiento())
                .isTrue();
        assertThat(
                        fixture.estado
                                .estado(1L, fixture.evaluacionesCreadas.getFirst().getFechaCorte())
                                .aptoParaEntrenamiento())
                .isFalse();
        assertThat(preparacion.filas())
                .filteredOn(fila -> fila.evaluacionId() < 5)
                .allMatch(fila -> !fila.aptoParaEntrenamientoCompleto());
        assertThat(preparacion.filas())
                .filteredOn(fila -> fila.evaluacionId() == 5)
                .singleElement()
                .satisfies(fila -> assertThat(fila.aptoParaEntrenamientoCompleto()).isTrue());
        assertThat(fixture.dataset.csv())
                .contains(
                        "case_id",
                        "cliente_group_id",
                        "fecha_corte",
                        "schema_version",
                        "rubrica_codigo",
                        "rubrica_version",
                        "clasificacion_real")
                .doesNotContain("cliente_id", "evaluacion_id");
    }

    @Test
    void ceroFilasAptasConservaCsvConEncabezado() {
        Fixture fixture = fixture();
        when(fixture.evaluaciones.findAllByOrderByIdAsc())
                .thenReturn(fixture.evaluacionesCreadas.subList(0, 4));

        String csv = fixture.dataset.csv();

        assertThat(fixture.dataset.filas()).isEmpty();
        assertThat(csv.lines()).hasSize(1);
        assertThat(csv).contains("case_id", "rubrica_codigo", "edad", "clasificacion_real");
    }

    private Fixture fixture() {
        EvaluacionPerfilHabitosRepository evaluaciones =
                mock(EvaluacionPerfilHabitosRepository.class);
        ClienteRepository clientes = mock(ClienteRepository.class);
        VariablesModeloV5Service variables = mock(VariablesModeloV5Service.class);
        FeatureSchemaV5Mapper mapper = mock(FeatureSchemaV5Mapper.class);
        CompletitudVentanaV5Service completitud = mock(CompletitudVentanaV5Service.class);
        AptitudEntrenamientoV5Service aptitud = new AptitudEntrenamientoV5Service();
        LocalDate corte = LocalDate.now();
        var smoke =
                escenario(
                        1L,
                        corte,
                        "SMOKE TECNICO DATASET V5",
                        false,
                        EstadoValidezMedicion.VALIDA,
                        EstadoRubricaPerfilHabitos.ACTIVA,
                        variables,
                        mapper);
        var xNull =
                escenario(
                        2L,
                        corte.minusDays(1),
                        null,
                        true,
                        EstadoValidezMedicion.VALIDA,
                        EstadoRubricaPerfilHabitos.ACTIVA,
                        variables,
                        mapper);
        var groundTruthInvalido =
                escenario(
                        3L,
                        corte.minusDays(2),
                        null,
                        false,
                        EstadoValidezMedicion.INVALIDA,
                        EstadoRubricaPerfilHabitos.ACTIVA,
                        variables,
                        mapper);
        var rubricaInactiva =
                escenario(
                        4L,
                        corte.minusDays(3),
                        null,
                        false,
                        EstadoValidezMedicion.VALIDA,
                        EstadoRubricaPerfilHabitos.INACTIVA,
                        variables,
                        mapper);
        var valida =
                escenario(
                        5L,
                        corte.minusDays(4),
                        null,
                        false,
                        EstadoValidezMedicion.VALIDA,
                        EstadoRubricaPerfilHabitos.ACTIVA,
                        variables,
                        mapper);
        List<EvaluacionPerfilHabitos> lista =
                List.of(smoke, xNull, groundTruthInvalido, rubricaInactiva, valida);
        when(evaluaciones.findAllByOrderByIdAsc()).thenReturn(lista);
        when(clientes.count()).thenReturn(5L);
        for (EvaluacionPerfilHabitos evaluacion : lista) {
            Long clienteId = evaluacion.getCliente().getId();
            LocalDate fechaCorte = evaluacion.getFechaCorte();
            for (DominioObservacionDiaria dominio : DominioObservacionDiaria.values()) {
                when(completitud.verificar(clienteId, fechaCorte, dominio))
                        .thenReturn(
                                new CompletitudVentanaV5Response(
                                        true,
                                        7,
                                        7,
                                        fechaCorte.minusDays(6),
                                        fechaCorte));
            }
        }
        return new Fixture(
                evaluaciones,
                lista,
                new DatasetModeloV5Service(evaluaciones, variables, mapper, aptitud),
                new PreparacionDatasetModeloV5Service(
                        clientes, evaluaciones, variables, mapper, aptitud),
                new EstadoPreparacionClienteV5Service(
                        variables, mapper, completitud, evaluaciones, aptitud));
    }

    private EvaluacionPerfilHabitos escenario(
            Long id,
            LocalDate corte,
            String observacion,
            boolean unaXNull,
            EstadoValidezMedicion validez,
            EstadoRubricaPerfilHabitos estadoRubrica,
            VariablesModeloV5Service variables,
            FeatureSchemaV5Mapper mapper) {
        Cliente cliente = mock(Cliente.class);
        when(cliente.getId()).thenReturn(id);
        when(cliente.getDatasetGroupId()).thenReturn(UUID.randomUUID());
        RubricaPerfilHabitos rubrica = mock(RubricaPerfilHabitos.class);
        when(rubrica.getCodigo()).thenReturn("RUBRICA_REAL_V5");
        when(rubrica.getVersion()).thenReturn(java.math.BigDecimal.valueOf(3));
        when(rubrica.getEstado()).thenReturn(estadoRubrica);
        when(rubrica.getValidadoPor()).thenReturn("Especialista");
        when(rubrica.getValidadoEn()).thenReturn(Instant.parse("2026-08-01T10:00:00Z"));
        when(rubrica.getVigenteDesde()).thenReturn(Instant.parse("2026-08-01T10:00:00Z"));
        EvaluacionPerfilHabitos evaluacion = mock(EvaluacionPerfilHabitos.class);
        when(evaluacion.getId()).thenReturn(id);
        when(evaluacion.getCliente()).thenReturn(cliente);
        when(evaluacion.getRubrica()).thenReturn(rubrica);
        when(evaluacion.getFechaCorte()).thenReturn(corte);
        when(evaluacion.getEstadoValidez()).thenReturn(validez);
        when(evaluacion.getClasificacionReal()).thenReturn(ClasificacionPerfilHabitos.ADECUADO);
        when(evaluacion.getObservacion()).thenReturn(observacion);
        var metadata =
                new VariablesModeloV5Response.Metadata(
                        id, cliente.getDatasetGroupId(), "HISTORIAL", true, true, true);
        VariablesModeloV5Response respuesta = mock(VariablesModeloV5Response.class);
        when(respuesta.schemaVersion()).thenReturn(FeatureSchemaV5Mapper.SCHEMA_VERSION);
        when(respuesta.fechaCorte()).thenReturn(corte);
        when(respuesta.metadata()).thenReturn(metadata);
        var x = new LinkedHashMap<String, Object>();
        FeatureSchemaV5Mapper.FEATURE_NAMES.forEach(nombre -> x.put(nombre, 1));
        if (unaXNull) {
            x.put("promedio_kcal_7d", null);
        }
        when(variables.construir(id, corte)).thenReturn(respuesta);
        when(mapper.mapear(respuesta))
                .thenReturn(new FeatureSchemaV5(FeatureSchemaV5Mapper.SCHEMA_VERSION, x));
        return evaluacion;
    }

    private record Fixture(
            EvaluacionPerfilHabitosRepository evaluaciones,
            List<EvaluacionPerfilHabitos> evaluacionesCreadas,
            DatasetModeloV5Service dataset,
            PreparacionDatasetModeloV5Service preparacion,
            EstadoPreparacionClienteV5Service estado) {}
}
