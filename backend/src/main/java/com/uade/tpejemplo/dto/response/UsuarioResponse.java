package com.uade.tpejemplo.dto.response;

import com.uade.tpejemplo.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioResponse {

    private Long id;
    private String username;
    private String rol;
    private boolean puedeAnularCredito;
    private boolean puedeAnularCobranza;

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
            usuario.getId(),
            usuario.getUsername(),
            usuario.getRol().name(),
            usuario.isPuedeAnularCredito(),
            usuario.isPuedeAnularCobranza()
        );
    }
}