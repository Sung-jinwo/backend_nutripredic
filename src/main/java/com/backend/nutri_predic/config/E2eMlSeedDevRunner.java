package com.backend.nutri_predic.config;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.enums.Rol;
import com.backend.nutri_predic.alimentacion.habito.entity.RegistroHabito;
import com.backend.nutri_predic.alimentacion.habito.repository.RegistroHabitoRepository;
import com.backend.nutri_predic.suplemento.entity.SuplementoCatalogo;
import com.backend.nutri_predic.suplemento.entity.SuplementoCliente;
import com.backend.nutri_predic.suplemento.repository.SuplementoCatalogoRepository;
import com.backend.nutri_predic.suplemento.repository.SuplementoClienteRepository;
import com.backend.nutri_predic.usuario.entity.Usuario;
import com.backend.nutri_predic.usuario.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "test"})
public class E2eMlSeedDevRunner {
    private static final Logger log = LoggerFactory.getLogger(E2eMlSeedDevRunner.class);
    private static final LocalDate CORTE = LocalDate.of(2026, 8, 20);

    @Bean
    CommandLineRunner seedE2eMl(
            UsuarioRepository usuarios,
            ClienteRepository clientes,
            RegistroHabitoRepository habitos,
            SuplementoCatalogoRepository catalogo,
            SuplementoClienteRepository asignaciones) {
        return args -> {
            log.info("E2E ML SEED RUNNER START");
            var suplementos = asegurarCatalogo(catalogo);
            List<Caso> casos =
                    List.of(
                            new Caso("CP-01", 24, 68, 172, 4, 2.5, 7, 1, 4, 3, 1, 1),
                            new Caso("CP-02", 27, 94, 170, 2, .8, 1, 6, 1, 7, 5, 5),
                            new Caso("CP-03", 30, 76, 175, 3, 1.7, 4, 4, 2, 4, 2, 2),
                            new Caso("CP-04", 22, 72, 174, 4, 1.9, 5, 3, 3, 4, 2, 2),
                            new Caso("CP-05", 35, 85, 172, 3, 1.2, 3, 5, 1, 6, 4, 3),
                            new Caso("CP-06", 26, 74, 178, 4, 2.6, 6, 6, 4, 7, 5, 4),
                            new Caso("CP-07", 29, null, 176, 3, null, 4, 0, 2, 4, 2, 1),
                            new Caso("CP-08", null, null, null, 0, null, 0, 0, 0, 0, 0, 0));
            for (Caso c : casos) {
                String email = c.codigo.toLowerCase() + "@e2e.nutripredic.local";
                if (usuarios.existsByEmail(email)) {
                    log.info("E2E ML SEED {} EXISTS", c.codigo);
                    continue;
                }
                var u =
                        usuarios.save(
                                new Usuario(
                                        email,
                                        "E2E-NO-LOGIN",
                                        "DATOS DE PRUEBA E2E ML - " + c.codigo,
                                        Rol.CLIENTE));
                var cliente = new Cliente(u);
                cliente.setEdad(c.edad);
                if (c.peso != null) cliente.setPesoKg(BigDecimal.valueOf(c.peso));
                if (c.altura != null) cliente.setAlturaCm(BigDecimal.valueOf(c.altura));
                cliente.setObjetivoFisico("DATOS DE PRUEBA E2E ML - NO GROUND TRUTH");
                cliente = clientes.save(cliente);
                for (int i = 0; i < 7 && !c.codigo.equals("CP-08"); i++) {
                    var h = new RegistroHabito();
                    h.setCliente(cliente);
                    h.setFecha(CORTE.minusDays(6 - i));
                    h.setCantidadComidas(c.comidas);
                    h.setConsumoAgua(c.agua);
                    h.setProteinas(0d);
                    h.setTipoAlimentacion("TECNICO");
                    h.setNivelOrganizacion("TECNICO");
                    h.setDesayuno(i < c.desayunos);
                    h.setSnacks(i < c.snacks);
                    h.setComidasCocinadas(c.cocinadas);
                    h.setConsumeSuplementos(i < c.consumos);
                    habitos.save(h);
                }
                for (int i = 0; i < c.aplicables; i++) {
                    var a = new SuplementoCliente();
                    a.setCliente(cliente);
                    a.setSuplemento(suplementos.get(i));
                    a.setCantidad(1d);
                    a.setUnidad("UNIDAD");
                    a.setFrecuencia("DIARIA");
                    a.setTiempoUso("TÉCNICO");
                    a.setActivo(i < c.activos);
                    a.setFechaInicio(CORTE.minusDays(30));
                    asignaciones.save(a);
                }
                log.info("E2E ML SEED {} CREATED", c.codigo);
            }
            log.info("E2E ML SEED RUNNER END");
        };
    }

    private List<SuplementoCatalogo> asegurarCatalogo(SuplementoCatalogoRepository repo) {
        while (repo.findAll().size() < 5) {
            var s = new SuplementoCatalogo();
            s.setNombre("Suplemento técnico E2E " + (repo.findAll().size() + 1));
            s.setTipo("TECNICO");
            s.setActivo(true);
            repo.save(s);
        }
        return repo.findAll().subList(0, 5);
    }

    private record Caso(
            String codigo,
            Integer edad,
            Integer peso,
            Integer altura,
            int comidas,
            Double agua,
            int desayunos,
            int snacks,
            int cocinadas,
            int consumos,
            int aplicables,
            int activos) {}
}
