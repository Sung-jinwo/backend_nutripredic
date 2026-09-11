package com.backend.nutri_predic.usuario.service;

import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.usuario.dto.PerfilResponse;
import com.backend.nutri_predic.usuario.dto.UsuarioRequest;
import com.backend.nutri_predic.usuario.dto.UsuarioResponse;
import com.backend.nutri_predic.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;

    public UsuarioService(UsuarioRepository usuarios, ClienteRepository clientes) {
        this.usuarios = usuarios;
        this.clientes = clientes;
    }

    @Transactional(readOnly = true)
    public PerfilResponse current(String email) {
        var usuario =
                usuarios.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuario"));
        var cliente = clientes.findByUsuarioId(usuario.getId()).orElse(null);
        return PerfilResponse.from(usuario, cliente);
    }

    @Transactional
    public UsuarioResponse update(Long id, UsuarioRequest request) {
        var usuario =
                usuarios.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario"));
        if (request.nombre() != null) usuario.setNombre(request.nombre().trim());
        if (request.activo() != null) usuario.setActivo(request.activo());
        return UsuarioResponse.from(usuarios.save(usuario));
    }
}
