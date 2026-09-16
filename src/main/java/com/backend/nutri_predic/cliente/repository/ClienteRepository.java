package com.backend.nutri_predic.cliente.repository;

import com.backend.nutri_predic.cliente.entity.Cliente;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from Cliente c where c.id = :id")
    Optional<Cliente> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
    Optional<Cliente> findByUsuarioId(Long usuarioId);
}
