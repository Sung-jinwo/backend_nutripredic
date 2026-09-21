package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.cliente.peso.dto.RegistroPesoRequest;
import com.backend.nutri_predic.cliente.peso.entity.RegistroPesoCliente;
import com.backend.nutri_predic.cliente.peso.repository.RegistroPesoClienteRepository;
import com.backend.nutri_predic.cliente.peso.service.RegistroPesoClienteService;
import com.backend.nutri_predic.cliente.service.ClienteService;
import com.backend.nutri_predic.common.exception.ConflictException;
import com.backend.nutri_predic.common.service.AccessService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class RegistroPesoClienteServiceTests {
    private final RegistroPesoClienteRepository registros = mock(RegistroPesoClienteRepository.class);
    private final AccessService access = mock(AccessService.class);
    private final ClienteService clientes = mock(ClienteService.class);
    private final Authentication auth = mock(Authentication.class);
    private final com.backend.nutri_predic.cliente.repository.ClienteRepository clienteRepository = mock(com.backend.nutri_predic.cliente.repository.ClienteRepository.class);
    private RegistroPesoClienteService servicio;
    private Cliente cliente;
    private LocalDate hoy;

    @BeforeEach
    void preparar() {
        reset(registros, access, clientes, auth, clienteRepository);
        servicio = new RegistroPesoClienteService(registros, access, clientes, clienteRepository);
        cliente = new Cliente();
        cliente.setPesoKg(new BigDecimal("70.00"));
        hoy = LocalDate.now(ZoneId.of("America/Lima"));
        when(access.client(1L, auth)).thenReturn(cliente);
        when(clienteRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(cliente));
        when(registros.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void rechazaUnaNuevaMedicionAntesDeSieteDias() {
        var ultimo = registro(hoy.minusDays(3), "70.00", null);
        when(registros.findByClienteIdAndFechaMedicion(1L, hoy)).thenReturn(Optional.empty());
        when(registros.findFirstByClienteIdOrderByFechaMedicionDescIdDesc(1L)).thenReturn(Optional.of(ultimo));

        assertThatThrownBy(() -> servicio.registrar(
                1L, new RegistroPesoRequest(new BigDecimal("69.50"), false), auth))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("PESO_SEMANAL_AUN_NO_DISPONIBLE");
        verifyNoInteractions(clientes);
    }

    @Test
    void exigeConfirmacionCuandoLaVariacionEsCincoPorCientoOMas() {
        var anterior = registro(hoy.minusDays(7), "70.00", null);
        when(registros.findByClienteIdAndFechaMedicion(1L, hoy)).thenReturn(Optional.empty());
        when(registros.findFirstByClienteIdOrderByFechaMedicionDescIdDesc(1L)).thenReturn(Optional.of(anterior));
        when(registros.findFirstByClienteIdAndFechaMedicionBeforeOrderByFechaMedicionDescIdDesc(1L, hoy))
                .thenReturn(Optional.of(anterior));

        assertThatThrownBy(() -> servicio.registrar(
                1L, new RegistroPesoRequest(new BigDecimal("73.50"), false), auth))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("CONFIRMAR_CAMBIO_PESO");
    }

    @Test
    void rechazaUnSegundoGuardadoElMismoDiaSinSobrescribir() {
        var mismoDia = registro(hoy, "72.00", "70.00");
        when(registros.findByClienteIdAndFechaMedicion(1L, hoy)).thenReturn(Optional.of(mismoDia));
        when(registros.findFirstByClienteIdAndFechaMedicionBeforeOrderByFechaMedicionDescIdDesc(1L, hoy))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.registrar(
                1L, new RegistroPesoRequest(new BigDecimal("73.00"), false), auth))
                .isInstanceOf(ConflictException.class).hasMessageContaining("PESO_SEMANAL_YA_REGISTRADO");
        verify(registros, never()).save(any());
        verifyNoInteractions(clientes);
    }

    @Test void permiteMedirAlSeptimoDia() {
        var anterior = registro(hoy.minusDays(7), "70.00", null);
        when(registros.findFirstByClienteIdOrderByFechaMedicionDescIdDesc(1L)).thenReturn(Optional.of(anterior));
        when(registros.findFirstByClienteIdAndFechaMedicionBeforeOrderByFechaMedicionDescIdDesc(1L, hoy)).thenReturn(Optional.of(anterior));
        var respuesta = servicio.registrar(1L, new RegistroPesoRequest(new BigDecimal("69.50"), false), auth);
        assertThat(respuesta.variacionKg()).isEqualByComparingTo("-0.50");
        assertThat(respuesta.proximaFecha()).isEqualTo(hoy.plusDays(7));
        assertThat(respuesta.habilitado()).isFalse();
        var orden = inOrder(clienteRepository, registros);
        orden.verify(clienteRepository).findByIdForUpdate(1L);
        orden.verify(registros).findByClienteIdAndFechaMedicion(1L, hoy);
    }

    private RegistroPesoCliente registro(LocalDate fecha, String peso, String pesoAnterior) {
        var registro = new RegistroPesoCliente();
        registro.setCliente(cliente);
        registro.setFechaMedicion(fecha);
        registro.setPesoKg(new BigDecimal(peso));
        if (pesoAnterior != null) registro.setPesoAnteriorKg(new BigDecimal(pesoAnterior));
        return registro;
    }

    @Test void unaRepeticionIdenticaNoGuardaNiActualizaElPerfilOtraVez() {
        when(registros.save(any())).thenAnswer(invocacion -> {
            RegistroPesoCliente guardado = invocacion.getArgument(0);
            when(registros.findByClienteIdAndFechaMedicion(1L, hoy)).thenReturn(Optional.of(guardado));
            return guardado;
        });
        var request = new RegistroPesoRequest(new BigDecimal("70.00"), false);
        servicio.registrar(1L, request, auth);
        assertThatThrownBy(() -> servicio.registrar(1L, request, auth))
                .isInstanceOf(ConflictException.class).hasMessageContaining("PESO_SEMANAL_YA_REGISTRADO");
        verify(registros, times(1)).save(any());
        verify(clientes, times(1)).actualizarPesoSemanal(1L, request.pesoKg(), hoy);
    }

    @Test void historialConsultaSoloAlClienteAutorizado() {
        when(registros.findByClienteIdOrderByFechaMedicionDescIdDesc(1L))
                .thenReturn(java.util.List.of(registro(hoy.minusDays(7), "69.50", "70.00")));
        var historial = servicio.historial(1L, auth);
        assertThat(historial).hasSize(1);
        assertThat(historial.getFirst().fechaMedicion()).isEqualTo(hoy.minusDays(7));
        verify(access).client(1L, auth);
    }

    @Test void rechazaPesoFueraDeRangoYMasDeDosDecimalesEnElContrato() {
        try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (String valor : java.util.List.of("0", "501", "70.123"))
                assertThat(validator.validate(new RegistroPesoRequest(new BigDecimal(valor), false))).isNotEmpty();
            assertThat(validator.validate(new RegistroPesoRequest(new BigDecimal("70.25"), false))).isEmpty();
        }
    }
}
