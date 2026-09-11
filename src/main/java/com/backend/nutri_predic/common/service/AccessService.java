package com.backend.nutri_predic.common.service;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.*;
import org.springframework.stereotype.Service;

@Service
public class AccessService {
    private final ClienteRepository clients;

    public AccessService(ClienteRepository c) {
        clients = c;
    }

    public Cliente client(Long id, Authentication a) {
        var c = clients.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente"));
        if (a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_ADMIN"))
                || c.getUsuario().getEmail().equals(a.getName())) return c;
        throw new AccessDeniedException("No puedes acceder a los datos de otro cliente");
    }
}
