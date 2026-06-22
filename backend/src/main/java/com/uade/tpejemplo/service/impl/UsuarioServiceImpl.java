package com.uade.tpejemplo.service.impl;

import com.uade.tpejemplo.dto.request.PermisosRequest;
import com.uade.tpejemplo.dto.response.UsuarioResponse;
import com.uade.tpejemplo.exception.BusinessException;
import com.uade.tpejemplo.exception.ResourceNotFoundException;
import com.uade.tpejemplo.model.Rol;
import com.uade.tpejemplo.model.Usuario;
import com.uade.tpejemplo.repository.UsuarioRepository;
import com.uade.tpejemplo.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Override
    // CAMBIO: el gestor de permisos trabaja solo sobre usuarios con rol USER.
    public List<UsuarioResponse> listarUsuariosGestionables() {
        return usuarioRepository.findByRolOrderByUsernameAsc(Rol.USER).stream()
            .map(UsuarioResponse::from)
            .toList();
    }

    @Override
    @Transactional
    // CAMBIO: actualización centralizada de permisos para evitar exponer password u otros datos sensibles.
    public UsuarioResponse actualizarPermisos(Long id, PermisosRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (usuario.getRol() != Rol.USER) {
            throw new BusinessException("Solo se pueden administrar permisos de usuarios con rol USER.");
        }

        usuario.setPuedeAnularCredito(Boolean.TRUE.equals(request.getPuedeAnularCredito()));
        usuario.setPuedeAnularCobranza(Boolean.TRUE.equals(request.getPuedeAnularCobranza()));
        usuarioRepository.save(usuario);

        return UsuarioResponse.from(usuario);
    }
}