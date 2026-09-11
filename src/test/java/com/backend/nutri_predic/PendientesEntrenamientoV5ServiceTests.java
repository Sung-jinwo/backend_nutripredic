package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.datasetmodelov5.dto.EstadoPreparacionClienteV5Response;
import com.backend.nutri_predic.datasetmodelov5.service.AptitudEntrenamientoV5Service;
import com.backend.nutri_predic.datasetmodelov5.service.DatasetModeloV5Service;
import com.backend.nutri_predic.datasetmodelov5.service.EstadoPreparacionClienteV5Service;
import com.backend.nutri_predic.datasetmodelov5.service.PendientesEntrenamientoV5Service;
import com.backend.nutri_predic.perfilhabitos.entity.ClasificacionPerfilHabitos;
import com.backend.nutri_predic.perfilhabitos.entity.EstadoRubricaPerfilHabitos;
import com.backend.nutri_predic.perfilhabitos.entity.EvaluacionPerfilHabitos;
import com.backend.nutri_predic.perfilhabitos.entity.RubricaPerfilHabitos;
import com.backend.nutri_predic.perfilhabitos.repository.EvaluacionPerfilHabitosRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PendientesEntrenamientoV5ServiceTests {

    @Test
    void listaSoloPendientesNoSmokeYPriorizaGroundTruthValidoAntesQueCantidadX() {
        LocalDate corte = LocalDate.of(2026, 8, 30);
        ClienteRepository clientes = mock(ClienteRepository.class);
        EvaluacionPerfilHabitosRepository evaluaciones =
                mock(EvaluacionPerfilHabitosRepository.class);
        EstadoPreparacionClienteV5Service estados = mock(EstadoPreparacionClienteV5Service.class);
        DatasetModeloV5Service dataset = mock(DatasetModeloV5Service.class);
        AptitudEntrenamientoV5Service aptitud = new AptitudEntrenamientoV5Service();
        List<Cliente> listaClientes = List.of(cliente(1L), cliente(2L), cliente(3L), cliente(4L));
        var smoke = evaluacion(1L, listaClientes.getFirst(), corte.minusDays(1), true);
        var groundTruthValido = evaluacion(2L, listaClientes.get(1), corte, false);
        var filaApta = evaluacion(4L, listaClientes.get(3), corte, false);
        when(clientes.findAll()).thenReturn(listaClientes);
        when(clientes.count()).thenReturn(4L);
        when(evaluaciones.findAllByOrderByIdAsc())
                .thenReturn(List.of(smoke, groundTruthValido, filaApta));
        when(dataset.filas()).thenReturn(List.of());
        when(estados.estado(1L, corte)).thenReturn(estado(1L, corte, 21, true, false, Map.of()));
        when(estados.estado(2L, corte))
                .thenReturn(
                        estado(
                                2L,
                                corte,
                                19,
                                false,
                                false,
                                Map.of(
                                        "promedio_creatina_g_7d",
                                        "VENTANA_SUPLEMENTACION_INCOMPLETA",
                                        "promedio_cafeina_mg_7d",
                                        "VENTANA_SUPLEMENTACION_INCOMPLETA")));
        when(estados.estado(3L, corte))
                .thenReturn(
                        estado(
                                3L,
                                corte,
                                20,
                                false,
                                false,
                                Map.of("tipo_objetivo_fisico", "OBJETIVO_FISICO_FALTANTE")));
        when(estados.estado(4L, corte)).thenReturn(estado(4L, corte, 21, true, true, Map.of()));
        var service =
                new PendientesEntrenamientoV5Service(
                        clientes, evaluaciones, estados, dataset, aptitud);

        var reporte = service.listar(corte);

        assertThat(reporte.candidatos()).extracting(c -> c.clienteId()).containsExactly(2L, 3L);
        assertThat(reporte.candidatos()).extracting(c -> c.prioridadOrden()).containsExactly(1, 2);
        assertThat(reporte.candidatos().getFirst().groundTruth().groundTruthValido()).isTrue();
        assertThat(reporte.candidatos().getFirst().datosObservacionales().xDisponibles())
                .isEqualTo(19);
        assertThat(reporte.candidatos().get(1).groundTruth().disponible()).isFalse();
        assertThat(reporte.candidatos().get(1).datosObservacionales().perfilFaltante()).isFalse();
        assertThat(reporte.candidatos().get(1).datosObservacionales().objetivoFisicoFaltante())
                .isTrue();
        assertThat(reporte.candidatos().getFirst().datosObservacionales().featuresXFaltantes())
                .containsExactly("promedio_cafeina_mg_7d", "promedio_creatina_g_7d");
        assertThat(reporte.resumen().filasTrainables()).isZero();
        assertThat(reporte.resumen().totalExcluidosSmokeTecnico()).isEqualTo(1);
        assertThat(reporte.resumen().totalAptosOmitidos()).isEqualTo(1);
        assertThat(reporte.resumen().datasetV5ListoParaEntrenar()).isFalse();
    }

    private Cliente cliente(Long id) {
        Cliente cliente = mock(Cliente.class);
        when(cliente.getId()).thenReturn(id);
        return cliente;
    }

    private EvaluacionPerfilHabitos evaluacion(
            Long id, Cliente cliente, LocalDate corte, boolean smoke) {
        RubricaPerfilHabitos rubrica = mock(RubricaPerfilHabitos.class);
        when(rubrica.getEstado()).thenReturn(EstadoRubricaPerfilHabitos.ACTIVA);
        when(rubrica.getCodigo()).thenReturn("RUBRICA_REAL_V5");
        when(rubrica.getVersion()).thenReturn(java.math.BigDecimal.valueOf(1));
        when(rubrica.getValidadoPor()).thenReturn("Especialista");
        when(rubrica.getValidadoEn()).thenReturn(Instant.parse("2026-08-01T10:00:00Z"));
        when(rubrica.getVigenteDesde()).thenReturn(Instant.parse("2026-08-01T10:00:00Z"));
        EvaluacionPerfilHabitos evaluacion = mock(EvaluacionPerfilHabitos.class);
        when(evaluacion.getId()).thenReturn(id);
        when(evaluacion.getCliente()).thenReturn(cliente);
        when(evaluacion.getFechaCorte()).thenReturn(corte);
        when(evaluacion.getRubrica()).thenReturn(rubrica);
        when(evaluacion.getEstadoValidez()).thenReturn(EstadoValidezMedicion.VALIDA);
        when(evaluacion.getClasificacionReal()).thenReturn(ClasificacionPerfilHabitos.ADECUADO);
        when(evaluacion.getObservacion()).thenReturn(smoke ? "SMOKE TECNICO DATASET V5" : null);
        return evaluacion;
    }

    private EstadoPreparacionClienteV5Response estado(
            Long id,
            LocalDate corte,
            int xDisponibles,
            boolean smoke,
            boolean apto,
            Map<String, String> motivos) {
        return new EstadoPreparacionClienteV5Response(
                id,
                corte,
                !motivos.containsValue("PERFIL_FALTANTE")
                        && !motivos.containsKey("tipo_objetivo_fisico"),
                7,
                xDisponibles < 21 ? 6 : 7,
                7,
                7,
                5,
                5,
                apto,
                smoke,
                xDisponibles,
                apto,
                apto ? List.of() : List.of("REGISTRAR_GROUND_TRUTH"),
                motivos);
    }
}
