package com.backend.nutri_predic.usuario.repository;

import com.backend.nutri_predic.usuario.entity.Usuario;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
