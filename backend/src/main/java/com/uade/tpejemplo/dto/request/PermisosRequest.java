package com.uade.tpejemplo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermisosRequest {

    @NotNull
    private Boolean puedeAnularCredito;

    @NotNull
    private Boolean puedeAnularCobranza;
}