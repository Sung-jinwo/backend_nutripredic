package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;

import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.enums.TipoObjetivoFisico;
import com.backend.nutri_predic.alimentacion.habito.entity.RegistroHabito;
import com.backend.nutri_predic.alimentacion.habito.repository.RegistroHabitoRepository;
import com.backend.nutri_predic.variablemodelov5.service.VariablesModeloV5Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class VariablesModeloV5ServiceIntegrationTests {
    @Autowired AuthService auth;
    @Autowired ClienteRepository clientes;
    @Autowired RegistroHabitoRepository habitos;
    @Autowired VariablesModeloV5Service variables;

    @Test
    @Transactional
    void calculaVentana7dDesdeDatosRealesSinImputar() {
        LocalDate corte = LocalDate.of(2026, 8, 28);
        Cliente cliente = cliente();
        for (int i = 0; i < 7; i++) {
            LocalDate fecha = corte.minusDays(6 - i);
            guardarHabito(cliente, fecha, i < 4);
        }

        // Completitud de ventana: stub siempre-completo (PENDIENTE_TECNICO:
        // EstadoObservacionDiaria real). V5 calcula desde datos presentes y
        // reporta null lo ausente, sin imputar.
        var completo = variables.construir(cliente.getId(), corte);
        assertThat(completo.schemaVersion()).isEqualTo("variables-modelo-v5");
        assertThat(completo.features().promedioCantidadComidas7d())
                .isEqualByComparingTo("3.000000");
        assertThat(completo.features().proporcionDesayuno7d()).isEqualByComparingTo("0.571429");
        assertThat(completo.features().promedioProteinaSuplementaria7d()).isZero();
        assertThat(completo.features().promedioCreatina7d()).isZero();
        assertThat(completo.features().promedioKcal7d()).isNull();
    }

    @Test
    @Transactional
    void clienteSinRegistrosReportaNulosSinImputar() {
        LocalDate corte = LocalDate.of(2026, 8, 28);
        Cliente cliente = cliente();

        var vacio = variables.construir(cliente.getId(), corte);
        assertThat(vacio.features().promedioCantidadComidas7d()).isNull();
        assertThat(vacio.features().promedioKcal7d()).isNull();
        // Sin consumos registrados el aporte suplementario promedia 0 (no null).
        assertThat(vacio.features().promedioProteinaSuplementaria7d()).isZero();
    }

    private Cliente cliente() {
        var registro =
                auth.register(
                        new RegisterRequest(
                                "v5-" + UUID.randomUUID() + "@test.local", "Password1!", "V5"));
        var cliente = clientes.findById(registro.clienteId()).orElseThrow();
        cliente.setEdad(29);
        cliente.setPesoKg(new BigDecimal("70.00"));
        cliente.setAlturaCm(new BigDecimal("175.00"));
        cliente.setTipoObjetivoFisico(TipoObjetivoFisico.GANAR_MASA_MUSCULAR);
        return clientes.save(cliente);
    }

    private void guardarHabito(Cliente cliente, LocalDate fecha, boolean desayuno) {
        var registro = new RegistroHabito();
        registro.setCliente(cliente);
        registro.setFecha(fecha);
        registro.setCantidadComidas(3);
        registro.setConsumoAgua(2.0);
        registro.setDesayuno(desayuno);
        registro.setSnacks(false);
        registro.setComidasCocinadas(2);
        registro.setConsumeSuplementos(false);
        habitos.save(registro);
    }
}
