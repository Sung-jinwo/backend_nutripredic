package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;

import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.consumo.dto.EvaluacionConsumoRequest;
import com.backend.nutri_predic.consumo.repository.EvaluacionConsumoRepository;
import com.backend.nutri_predic.consumo.service.ConsumoEvaluacionExtractor;
import com.backend.nutri_predic.dashboard.service.DashboardService;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class PcsLegacyIndicatorIsolationTests {
    private static final LocalDate CORTE = LocalDate.of(2026, 8, 20);

    @Autowired AuthService auth;
    @Autowired ClienteRepository clientes;
    @Autowired EvaluacionConsumoRepository evaluaciones;
    @Autowired ConsumoEvaluacionExtractor extractor;
    @Autowired DashboardService dashboard;

    @Test
    @Transactional
    void evaluacionExistenteNoDependeDeNivelesManuales() {
        Cliente cliente = cliente();
        var creada =
                extractor.extraer(new EvaluacionConsumoRequest(cliente.getId(), CORTE, 7, null));

        var recargada = evaluaciones.findById(creada.id()).orElseThrow();
        assertThat(recargada.getAltoConsumo()).isNull();
        assertThat(recargada.getEstadoClasificacion().name()).isEqualTo("NO_DETERMINADA");
    }

    @Test
    @Transactional
    void sinRubricaNoProduceClasificacionMetodologica() {
        Cliente cliente = cliente();

        var resultado =
                extractor.extraer(new EvaluacionConsumoRequest(cliente.getId(), CORTE, 7, null));

        assertThat(resultado.altoConsumo()).isNull();
        assertThat(resultado.estadoClasificacion()).isEqualTo("NO_DETERMINADA");
        assertThat(resultado.motivo()).isEqualTo("CRITERIO_NO_CONFIGURADO");
    }

    @Test
    @Transactional
    void dashboardSinOperativoManualYPcsOficialAislado() {
        cliente();

        // IndicadorService legacy eliminado: el dashboard solo expone oficiales.
        // El operativo manual queda en null por contrato y PCS sigue no disponible.
        var dashboardOficial = dashboard.dashboard();
        assertThat(dashboardOficial.porcentajeNivelConsumoOperativo()).isNull();
        assertThat(dashboardOficial.pcs().porcentajePcs()).isNull();
    }

    private Cliente cliente() {
        var registro =
                auth.register(
                        new RegisterRequest(
                                "pcs-legacy-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "PCS"));
        return clientes.findById(registro.clienteId()).orElseThrow();
    }
}
