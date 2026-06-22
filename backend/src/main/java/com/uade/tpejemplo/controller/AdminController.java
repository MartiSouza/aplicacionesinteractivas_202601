package com.uade.tpejemplo.controller;

import com.uade.tpejemplo.dto.request.PermisosRequest;
import com.uade.tpejemplo.dto.response.UsuarioResponse;
import com.uade.tpejemplo.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
// CAMBIO: el gestor de permisos queda restringido al rol ADMIN.
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UsuarioService usuarioService;

    // CAMBIO: listado de usuarios USER con permisos visibles para el gestor administrativo.
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuariosGestionables());
    }

    // CAMBIO: actualización inmediata de permisos individuales desde la pantalla admin.
    @PutMapping("/{id}/permisos")
    public ResponseEntity<UsuarioResponse> actualizarPermisos(@PathVariable Long id,
                                                              @Valid @RequestBody PermisosRequest request) {
        return ResponseEntity.ok(usuarioService.actualizarPermisos(id, request));
    }
}