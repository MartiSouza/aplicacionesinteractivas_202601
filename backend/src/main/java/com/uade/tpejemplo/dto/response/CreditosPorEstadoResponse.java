package com.uade.tpejemplo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreditosPorEstadoResponse {

    private String estado;
    private Long cantidad;
}