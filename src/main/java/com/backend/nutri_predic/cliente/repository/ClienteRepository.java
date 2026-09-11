package com.backend.nutri_predic.cliente.repository;

import com.backend.nutri_predic.cliente.entity.Cliente;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByUsuarioId(Long usuarioId);
}
