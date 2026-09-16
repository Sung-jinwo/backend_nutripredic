package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import com.backend.nutri_predic.config.AdminDemoDataSeeder;
import com.backend.nutri_predic.alimentacion.habito.repository.RegistroHabitoRepository;
import com.backend.nutri_predic.alimentacion.repository.RegistroAlimentoRepository;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.cliente.repository.HistorialPerfilClienteRepository;
import com.backend.nutri_predic.suplemento.repository.RegistroConsumoSuplementoRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {"app.demo-seed.enabled=false", "app.demo-seed.password=SeedOnlyTest123!"})
@Transactional
class AdminDemoDataSeederIntegrationTests {
    @Autowired AdminDemoDataSeeder seeder;
    @Autowired ClienteRepository clientes;
    @Autowired RegistroHabitoRepository habitos;
    @Autowired RegistroAlimentoRepository alimentos;
    @Autowired RegistroConsumoSuplementoRepository suplementos;
    @Autowired HistorialPerfilClienteRepository perfiles;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void creaEntradasRelacionadasUnaSolaVezSinSobrescribirElDia() {
        var caso = AdminDemoDataSeeder.CASES.getFirst();
        var fecha = LocalDate.of(2026, 9, 15);
        var id = seeder.seedInputs(caso, fecha);
        assertThat(seeder.seedInputs(caso, fecha)).isEqualTo(id);
        var cliente = clientes.findById(id).orElseThrow();
        assertThat(cliente.getUsuario().getNombre()).contains("DEMO");
        assertThat(passwordEncoder.matches("SeedOnlyTest123!", cliente.getUsuario().getPassword())).isTrue();
        assertThat(perfiles.findByClienteIdOrderByFechaDesdeDescCreadoEnDescIdDesc(id)).hasSize(1);
        var registros = habitos.findByClienteIdOrderByFechaDesc(id);
        assertThat(registros).hasSize(1);
        var h = registros.getFirst();
        assertThat(h.getConsumoAgua()).isEqualTo(1d);
        var comida = alimentos.findByRegistroHabitoIdOrderByIdAsc(h.getId());
        assertThat(comida).hasSize(1);
        assertThat(comida.getFirst().getProteinaGRegistrada()).isEqualByComparingTo("90");
        assertThat(suplementos.findByRegistroHabitoIdOrderByIdAsc(h.getId())).hasSize(1);
    }

    @Test
    void conservaHistoricoYRelacionDelSuplementoAlAgregarOtroDia() {
        var caso = AdminDemoDataSeeder.CASES.get(1);
        var fecha = LocalDate.of(2026, 9, 15);
        var id = seeder.seedInputs(caso, fecha);
        assertThat(seeder.seedInputs(caso, fecha.plusDays(1))).isEqualTo(id);
        var dias = habitos.findByClienteIdOrderByFechaDesc(id);
        assertThat(dias).hasSize(2);
        var primero = suplementos.findByRegistroHabitoIdOrderByIdAsc(dias.getFirst().getId()).getFirst();
        var anterior = suplementos.findByRegistroHabitoIdOrderByIdAsc(dias.getLast().getId()).getFirst();
        assertThat(primero.getSuplementoCliente().getId()).isEqualTo(anterior.getSuplementoCliente().getId());
        assertThat(anterior.getObservacion()).contains("NO_USAR_ENTRENAMIENTO");
        assertThat(perfiles.findByClienteIdOrderByFechaDesdeDescCreadoEnDescIdDesc(id)).hasSize(1);
    }
}
