package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.estudio.entity.*;
import com.backend.nutri_predic.estudio.repository.EstudioRepository;
import com.backend.nutri_predic.estudio.repository.ParticipacionEstudioRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class ParticipacionEstudioIntegrationTests {
    @Autowired AuthService auth;
    @Autowired ClienteRepository clientes;
    @Autowired EstudioRepository estudios;
    @Autowired ParticipacionEstudioRepository participaciones;
    @Autowired EntityManager entityManager;

    @Test
    @Transactional
    void clientePuedeParticiparConGrupoControlOExperimental() {
        var cliente1 = cliente("ctrl");
        var cliente2 = cliente("exp");
        var estudio = crearEstudio("EST-001");

        var p1 = new ParticipacionEstudio();
        p1.setEstudio(estudio);
        p1.setCliente(cliente1);
        p1.setCodigoParticipante("P001");
        p1.setGrupo(GrupoEstudio.CONTROL);
        p1.setFechaAsignacion(LocalDate.of(2026, 8, 1));
        p1.setEstado(EstadoEstudio.ACTIVO);
        participaciones.save(p1);

        var p2 = new ParticipacionEstudio();
        p2.setEstudio(estudio);
        p2.setCliente(cliente2);
        p2.setCodigoParticipante("P002");
        p2.setGrupo(GrupoEstudio.EXPERIMENTAL);
        p2.setFechaAsignacion(LocalDate.of(2026, 8, 1));
        p2.setEstado(EstadoEstudio.ACTIVO);
        participaciones.save(p2);

        entityManager.flush();
        entityManager.clear();

        var resultados =
                participaciones.findByEstudioIdOrderByCodigoParticipanteAsc(estudio.getId());
        assertThat(resultados).hasSize(2);
        assertThat(resultados.get(0).getGrupo()).isEqualTo(GrupoEstudio.CONTROL);
        assertThat(resultados.get(1).getGrupo()).isEqualTo(GrupoEstudio.EXPERIMENTAL);
        assertThat(resultados.get(0).getCliente().getId()).isEqualTo(cliente1.getId());
    }

    @Test
    @Transactional
    void mismoClienteNoPuedeDuplicarseEnMismoEstudio() {
        var cliente = cliente("dup");
        var estudio = crearEstudio("EST-DUP");

        var p1 = new ParticipacionEstudio();
        p1.setEstudio(estudio);
        p1.setCliente(cliente);
        p1.setCodigoParticipante("P001");
        p1.setGrupo(GrupoEstudio.CONTROL);
        p1.setFechaAsignacion(LocalDate.of(2026, 8, 1));
        p1.setEstado(EstadoEstudio.ACTIVO);
        participaciones.save(p1);
        entityManager.flush();

        assertThatThrownBy(
                        () -> {
                            var duplicado = new ParticipacionEstudio();
                            duplicado.setEstudio(estudio);
                            duplicado.setCliente(cliente);
                            duplicado.setCodigoParticipante("P002");
                            duplicado.setGrupo(GrupoEstudio.EXPERIMENTAL);
                            duplicado.setFechaAsignacion(LocalDate.of(2026, 8, 2));
                            duplicado.setEstado(EstadoEstudio.ACTIVO);
                            participaciones.save(duplicado);
                            entityManager.flush();
                        })
                .isInstanceOf(Exception.class);
    }

    @Test
    @Transactional
    void mismoClientePuedeParticiparEnOtroEstudioDistinto() {
        var cliente = cliente("multi");
        var estudio1 = crearEstudio("EST-M1");
        var estudio2 = crearEstudio("EST-M2");

        var p1 = new ParticipacionEstudio();
        p1.setEstudio(estudio1);
        p1.setCliente(cliente);
        p1.setCodigoParticipante("M-P001");
        p1.setGrupo(GrupoEstudio.CONTROL);
        p1.setFechaAsignacion(LocalDate.of(2026, 8, 1));
        p1.setEstado(EstadoEstudio.ACTIVO);
        participaciones.save(p1);

        var p2 = new ParticipacionEstudio();
        p2.setEstudio(estudio2);
        p2.setCliente(cliente);
        p2.setCodigoParticipante("M-P002");
        p2.setGrupo(GrupoEstudio.EXPERIMENTAL);
        p2.setFechaAsignacion(LocalDate.of(2026, 8, 2));
        p2.setEstado(EstadoEstudio.ACTIVO);
        participaciones.save(p2);

        entityManager.flush();
        entityManager.clear();

        assertThat(participaciones.existsByEstudioIdAndClienteId(estudio1.getId(), cliente.getId()))
                .isTrue();
        assertThat(participaciones.existsByEstudioIdAndClienteId(estudio2.getId(), cliente.getId()))
                .isTrue();
    }

    @Test
    @Transactional
    void grupoNoSeAlmacenaEnCliente() {
        var cliente = cliente("no-grupo");
        var estudio = crearEstudio("EST-NG");

        var p = new ParticipacionEstudio();
        p.setEstudio(estudio);
        p.setCliente(cliente);
        p.setCodigoParticipante("NG-P001");
        p.setGrupo(GrupoEstudio.EXPERIMENTAL);
        p.setFechaAsignacion(LocalDate.of(2026, 8, 1));
        p.setEstado(EstadoEstudio.ACTIVO);
        participaciones.save(p);
        entityManager.flush();

        entityManager.clear();
        var recargado = clientes.findById(cliente.getId()).orElseThrow();
        assertThat(recargado).isNotNull();
        assertThatThrownBy(() -> recargado.getClass().getMethod("getGrupo"))
                .isInstanceOf(NoSuchMethodException.class);
    }

    private Cliente cliente(String p) {
        var r =
                auth.register(
                        new RegisterRequest(
                                p + "-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "Estudio"));
        return clientes.findById(r.clienteId()).orElseThrow();
    }

    private Estudio crearEstudio(String codigo) {
        var e = new Estudio();
        e.setCodigo(codigo);
        e.setNombre("Estudio " + codigo);
        e.setEstado(EstadoEstudio.ACTIVO);
        e.setFechaInicio(LocalDate.of(2026, 1, 1));
        e.setFechaFin(LocalDate.of(2026, 12, 31));
        return estudios.save(e);
    }
}
