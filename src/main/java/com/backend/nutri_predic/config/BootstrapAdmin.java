package com.backend.nutri_predic.config;

import com.backend.nutri_predic.common.enums.Rol;
import com.backend.nutri_predic.usuario.entity.Usuario;
import com.backend.nutri_predic.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapAdmin {
    @Bean
    CommandLineRunner initialAdmin(
            UsuarioRepository usuarios,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email}") String email,
            @Value("${app.admin.password}") String password,
            @Value("${app.admin.nombre}") String nombre) {
        return args -> {
            if (!usuarios.existsByEmail(email)) {
                usuarios.save(
                        new Usuario(email, passwordEncoder.encode(password), nombre, Rol.ADMIN));
            }
        };
    }
}
