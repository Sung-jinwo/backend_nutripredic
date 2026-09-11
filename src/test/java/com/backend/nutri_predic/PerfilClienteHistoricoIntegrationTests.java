package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.nutri_predic.auth.dto.LoginRequest;
import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.cliente.dto.ClienteRequest;
import com.backend.nutri_predic.cliente.entity.HistorialPerfilCliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.cliente.repository.HistorialPerfilClienteRepository;
import com.backend.nutri_predic.cliente.service.ClienteService;
import com.backend.nutri_predic.cliente.service.PerfilClienteHistoricoService;
import com.backend.nutri_predic.common.enums.EstadoCliente;
import com.backend.nutri_predic.common.enums.Rol;
import com.backend.nutri_predic.common.enums.TipoObjetivoFisico;
import com.backend.nutri_predic.usuario.entity.Usuario;
import com.backend.nutri_predic.usuario.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class PerfilClienteHistoricoIntegrationTests {
    private static final UsernamePasswordAuthenticationToken ADMIN =
            new UsernamePasswordAuthenticationToken(
                    "admin@test.local", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    @Autowired AuthService auth;
    @Autowired ClienteRepository clientes;
    @Autowired HistorialPerfilClienteRepository historial;
    @Autowired ClienteService clienteService;
    @Autowired PerfilClienteHistoricoService perfiles;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired MockMvc mockMvc;

    @Test
    @Transactional
    void versionaObjetivoEImpideQueElFuturoContamineElCorteAnterior() {
        var cliente = cliente("temporal");
        LocalDate hoy = LocalDate.now();
        cliente.setEdad(30);
        cliente.setPesoKg(new BigDecimal("70"));
        cliente.setAlturaCm(new BigDecimal("170"));
        cliente.setObjetivoFisico("Mantener condición");
        cliente.setTipoObjetivoFisico(TipoObjetivoFisico.MANTENER_PESO);
        clientes.save(cliente);
        historial.save(new HistorialPerfilCliente(cliente, hoy));

        cliente.setObjetivoFisico("Ganar masa");
        cliente.setTipoObjetivoFisico(TipoObjetivoFisico.GANAR_MASA_MUSCULAR);
        clientes.save(cliente);
        historial.save(new HistorialPerfilCliente(cliente, hoy.plusDays(1)));

        var anterior = perfiles.resolver(cliente.getId(), hoy);
        var posterior = perfiles.resolver(cliente.getId(), hoy.plusDays(1));
        assertThat(anterior.fuentePerfil()).isEqualTo("HISTORIAL");
        assertThat(anterior.tipoObjetivoFisico()).isEqualTo(TipoObjetivoFisico.MANTENER_PESO);
        assertThat(posterior.tipoObjetivoFisico())
                .isEqualTo(TipoObjetivoFisico.GANAR_MASA_MUSCULAR);
    }

    @Test
    @Transactional
    void aceptaObjetivoControladoOtroYConservaLegacyNullConFallback() {
        var cliente = cliente("otro");
        clienteService.update(
                cliente.getId(),
                new ClienteRequest(
                        28,
                        new BigDecimal("65"),
                        new BigDecimal("165"),
                        "Objetivo técnico no taxonomizado",
                        TipoObjetivoFisico.OTRO,
                        EstadoCliente.ACTIVO),
                ADMIN);
        assertThat(
                        historial.findByClienteIdOrderByFechaDesdeDescCreadoEnDescIdDesc(
                                cliente.getId()))
                .singleElement()
                .satisfies(
                        version ->
                                assertThat(version.getTipoObjetivoFisico())
                                        .isEqualTo(TipoObjetivoFisico.OTRO));

        var legacy = cliente("legacy");
        var fallback = perfiles.resolver(legacy.getId(), LocalDate.now());
        assertThat(fallback.fuentePerfil()).isEqualTo("FALLBACK_ACTUAL");
        assertThat(fallback.tipoObjetivoFisico()).isNull();
    }

    @Test
    @Transactional
    void endpointAdminDevuelveCorteYContratoV4SigueSinCambios() throws Exception {
        var cliente = cliente("endpoint");
        LocalDate corte = LocalDate.now();
        clienteService.update(
                cliente.getId(),
                new ClienteRequest(
                        25,
                        new BigDecimal("60"),
                        new BigDecimal("160"),
                        "Mejorar rendimiento",
                        TipoObjetivoFisico.MEJORAR_RENDIMIENTO,
                        EstadoCliente.ACTIVO),
                ADMIN);
        String email = "admin-perfil-" + UUID.randomUUID() + "@test.local";
        usuarios.save(new Usuario(email, passwordEncoder.encode("Password1!"), "Admin", Rol.ADMIN));
        String token = auth.login(new LoginRequest(email, "Password1!")).token();

        mockMvc.perform(
                        get("/api/admin/clientes/{clienteId}/perfil-historico", cliente.getId())
                                .queryParam("fechaCorte", corte.toString())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(cliente.getId()))
                .andExpect(jsonPath("$.fechaCorte").value(corte.toString()))
                .andExpect(jsonPath("$.tipoObjetivoFisico").value("MEJORAR_RENDIMIENTO"))
                .andExpect(jsonPath("$.fuentePerfil").value("HISTORIAL"));
    }

    private com.backend.nutri_predic.cliente.entity.Cliente cliente(String prefijo) {
        var registro =
                auth.register(
                        new RegisterRequest(
                                prefijo + "-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "Cliente"));
        return clientes.findById(registro.clienteId()).orElseThrow();
    }
}
