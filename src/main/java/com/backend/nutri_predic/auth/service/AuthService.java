package com.backend.nutri_predic.auth.service;

import com.backend.nutri_predic.auth.dto.AuthResponse;
import com.backend.nutri_predic.auth.dto.LoginRequest;
import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.enums.Rol;
import com.backend.nutri_predic.common.exception.ConflictException;
import com.backend.nutri_predic.security.JwtService;
import com.backend.nutri_predic.usuario.entity.Usuario;
import com.backend.nutri_predic.usuario.repository.UsuarioRepository;
import java.util.Locale;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarios,
            ClienteRepository clientes,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByEmail(email)) {
            throw new ConflictException("El email ya está registrado");
        }
        Usuario usuario =
                usuarios.save(
                        new Usuario(
                                email,
                                passwordEncoder.encode(request.password()),
                                request.nombre().trim(),
                                Rol.CLIENTE));
        
        Cliente cliente = new Cliente(usuario);
        cliente.setSexo(request.sexo());

        if (Boolean.TRUE.equals(request.realizaActividadFisica())) {
            if (request.diasEntrenamientoSemana() == null
                    || request.diasEntrenamientoSemana() < 1
                    || request.diasEntrenamientoSemana() > 7)
                throw new com.backend.nutri_predic.common.exception.BusinessException(
                        "diasEntrenamientoSemana debe estar entre 1 y 7 cuando realizaActividadFisica es true");
            if (request.tipoActividadFisica() == null || request.tipoActividadFisica().isBlank())
                throw new com.backend.nutri_predic.common.exception.BusinessException(
                        "tipoActividadFisica es requerido cuando realizaActividadFisica es true");
            cliente.setRealizaActividadFisica(true);
            cliente.setDiasEntrenamientoSemana(request.diasEntrenamientoSemana());
            cliente.setTipoActividadFisica(request.tipoActividadFisica().trim());
        } else {
            // Paso 1 (registro mínimo) o sin actividad: campos de entrenamiento nulos.
            cliente.setRealizaActividadFisica(
                    request.realizaActividadFisica() == null ? null : false);
            cliente.setDiasEntrenamientoSemana(null);
            cliente.setTipoActividadFisica(null);
        }
        
        cliente = clientes.save(cliente);
        return response(usuario, cliente.getId());
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        Long clienteId = clientes.findByUsuarioId(usuario.getId()).map(Cliente::getId).orElse(null);
        return response(usuario, clienteId);
    }

    private AuthResponse response(Usuario usuario, Long clienteId) {
        return new AuthResponse(
                jwtService.generate(usuario.getEmail(), usuario.getRol().name()),
                "Bearer",
                usuario.getId(),
                clienteId,
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRol().name());
    }
}
