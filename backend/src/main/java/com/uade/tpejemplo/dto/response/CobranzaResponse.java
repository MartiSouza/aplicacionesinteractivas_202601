package com.uade.tpejemplo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CobranzaResponse {

    private Long id;
    private Long idCredito;
    private Integer idCuota;
    private BigDecimal importe;
    private LocalDate fechaCobranza;
    private boolean anulada;
}
