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
    private RegistroPesoClienteService servicio;
    private Cliente cliente;
    private LocalDate hoy;

    @BeforeEach
    void preparar() {
        reset(registros, access, clientes, auth);
        servicio = new RegistroPesoClienteService(registros, access, clientes);
        cliente = new Cliente();
        cliente.setPesoKg(new BigDecimal("70.00"));
        hoy = LocalDate.now(ZoneId.of("America/Lima"));
        when(access.client(1L, auth)).thenReturn(cliente);
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
    void corregirElMismoDiaConservaLaReferenciaSemanalOriginal() {
        var mismoDia = registro(hoy, "72.00", "70.00");
        when(registros.findByClienteIdAndFechaMedicion(1L, hoy)).thenReturn(Optional.of(mismoDia));
        when(registros.findFirstByClienteIdAndFechaMedicionBeforeOrderByFechaMedicionDescIdDesc(1L, hoy))
                .thenReturn(Optional.empty());

        var respuesta = servicio.registrar(
                1L, new RegistroPesoRequest(new BigDecimal("73.00"), false), auth);

        assertThat(respuesta.variacionKg()).isEqualByComparingTo("3.00");
        assertThat(respuesta.variacionPorcentual()).isEqualByComparingTo("4.29");
        verify(clientes).actualizarPesoSemanal(1L, new BigDecimal("73.00"), hoy);
    }

    private RegistroPesoCliente registro(LocalDate fecha, String peso, String pesoAnterior) {
        var registro = new RegistroPesoCliente();
        registro.setCliente(cliente);
        registro.setFechaMedicion(fecha);
        registro.setPesoKg(new BigDecimal(peso));
        if (pesoAnterior != null) registro.setPesoAnteriorKg(new BigDecimal(pesoAnterior));
        return registro;
    }
}
