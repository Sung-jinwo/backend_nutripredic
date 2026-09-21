package com.backend.nutri_predic.cliente.peso.repository;

import com.backend.nutri_predic.cliente.peso.entity.RegistroPesoCliente;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroPesoClienteRepository extends JpaRepository<RegistroPesoCliente, Long> {
    java.util.List<RegistroPesoCliente> findByClienteIdOrderByFechaMedicionDescIdDesc(Long clienteId);
    Optional<RegistroPesoCliente> findFirstByClienteIdOrderByFechaMedicionDescIdDesc(Long clienteId);
    Optional<RegistroPesoCliente> findByClienteIdAndFechaMedicion(Long clienteId, LocalDate fecha);
    Optional<RegistroPesoCliente> findFirstByClienteIdAndFechaMedicionBeforeOrderByFechaMedicionDescIdDesc(Long clienteId, LocalDate fecha);
}
