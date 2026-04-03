package com.uade.tpejemplo.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardResumenResponse {

    private Long totalCreditos;
    private BigDecimal montoTotalPrestado;
    private BigDecimal montoTotalCobrado;
    private BigDecimal porcentajeRecupero;
    private Long cantidadCreditosActivos;
    private Long cantidadCreditosEnMora;
    private List<CreditosPorEstadoResponse> creditosPorEstado;

    public DashboardResumenResponse(Long totalCreditos,
                                    BigDecimal montoTotalPrestado,
                                    BigDecimal montoTotalCobrado,
                                    BigDecimal porcentajeRecupero,
                                    Long cantidadCreditosActivos,
                                    Long cantidadCreditosEnMora,
                                    List<CreditosPorEstadoResponse> creditosPorEstado) {
        this.totalCreditos = totalCreditos != null ? totalCreditos : 0L;
        this.montoTotalPrestado = montoTotalPrestado != null ? montoTotalPrestado : BigDecimal.ZERO;
        this.montoTotalCobrado = montoTotalCobrado != null ? montoTotalCobrado : BigDecimal.ZERO;
        this.porcentajeRecupero = porcentajeRecupero != null ? porcentajeRecupero : BigDecimal.ZERO;
        this.cantidadCreditosActivos = cantidadCreditosActivos != null ? cantidadCreditosActivos : 0L;
        this.cantidadCreditosEnMora = cantidadCreditosEnMora != null ? cantidadCreditosEnMora : 0L;
        this.creditosPorEstado = creditosPorEstado != null ? creditosPorEstado : List.of();
    }
}