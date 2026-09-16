package com.backend.nutri_predic.cliente.peso.service;

import com.backend.nutri_predic.cliente.peso.dto.*;
import com.backend.nutri_predic.cliente.peso.entity.RegistroPesoCliente;
import com.backend.nutri_predic.cliente.peso.repository.RegistroPesoClienteRepository;
import com.backend.nutri_predic.cliente.service.ClienteService;
import com.backend.nutri_predic.common.exception.ConflictException;
import com.backend.nutri_predic.common.service.AccessService;
import java.math.*;
import java.time.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroPesoClienteService {
    private static final ZoneId ZONA = ZoneId.of("America/Lima");
    private static final BigDecimal UMBRAL_CONFIRMACION = new BigDecimal("5.00");
    private final RegistroPesoClienteRepository registros;
    private final AccessService access;
    private final ClienteService clientes;

    public RegistroPesoClienteService(RegistroPesoClienteRepository registros, AccessService access, ClienteService clientes) {
        this.registros = registros; this.access = access; this.clientes = clientes;
    }

    @Transactional(readOnly = true)
    public EstadoPesoSemanalResponse estado(Long clienteId, Authentication auth) {
        access.client(clienteId, auth);
        return EstadoPesoSemanalResponse.from(
                registros.findFirstByClienteIdOrderByFechaMedicionDescIdDesc(clienteId).orElse(null), LocalDate.now(ZONA));
    }

    @Transactional
    public EstadoPesoSemanalResponse registrar(Long clienteId, RegistroPesoRequest request, Authentication auth) {
        var cliente = access.client(clienteId, auth);
        LocalDate hoy = LocalDate.now(ZONA);
        var mismoDia = registros.findByClienteIdAndFechaMedicion(clienteId, hoy).orElse(null);
        var anterior = registros.findFirstByClienteIdAndFechaMedicionBeforeOrderByFechaMedicionDescIdDesc(clienteId, hoy).orElse(null);
        if (mismoDia == null) {
            var ultimo = registros.findFirstByClienteIdOrderByFechaMedicionDescIdDesc(clienteId).orElse(null);
            if (ultimo != null && hoy.isBefore(ultimo.getFechaMedicion().plusDays(7)))
                throw new ConflictException("PESO_SEMANAL_AUN_NO_DISPONIBLE: próximo registro " + ultimo.getFechaMedicion().plusDays(7));
        }
        BigDecimal referencia = mismoDia != null
                ? mismoDia.getPesoAnteriorKg()
                : anterior != null ? anterior.getPesoKg() : cliente.getPesoKg();
        BigDecimal variacion = referencia == null ? null : request.pesoKg().subtract(referencia).setScale(2, RoundingMode.HALF_UP);
        BigDecimal porcentaje = referencia == null || referencia.signum() == 0 ? null
                : variacion.abs().multiply(BigDecimal.valueOf(100)).divide(referencia, 2, RoundingMode.HALF_UP);
        if (porcentaje != null && porcentaje.compareTo(UMBRAL_CONFIRMACION) >= 0 && !request.confirmarCambioAnomalo())
            throw new ConflictException("CONFIRMAR_CAMBIO_PESO: la variación semanal es " + porcentaje.toPlainString() + "%");
        var registro = mismoDia == null ? new RegistroPesoCliente() : mismoDia;
        registro.setCliente(cliente); registro.setFechaMedicion(hoy); registro.setPesoKg(request.pesoKg());
        registro.setPesoAnteriorKg(referencia); registro.setVariacionKg(variacion);
        registro.setVariacionPorcentual(porcentaje); registro.setCambioAnomaloConfirmado(
                porcentaje != null && porcentaje.compareTo(UMBRAL_CONFIRMACION) >= 0);
        if (mismoDia != null) registro.setActualizadoEn(Instant.now());
        registro = registros.save(registro);
        clientes.actualizarPesoSemanal(clienteId, request.pesoKg(), hoy);
        return EstadoPesoSemanalResponse.from(registro, hoy);
    }
}
