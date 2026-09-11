package com.backend.nutri_predic.security;

import com.backend.nutri_predic.usuario.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UsuarioRepository users;

    public CustomUserDetailsService(UsuarioRepository users) {
        this.users = users;
    }

    public UserDetails loadUserByUsername(String email) {
        var u = users.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
        return User.withUsername(u.getEmail())
                .password(u.getPassword())
                .authorities("ROLE_" + u.getRol())
                .disabled(!u.isActivo())
                .build();
    }
}
