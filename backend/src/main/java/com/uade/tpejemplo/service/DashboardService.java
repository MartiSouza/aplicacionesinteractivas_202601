package com.uade.tpejemplo.service;

import com.uade.tpejemplo.dto.response.CreditosPorEstadoResponse;
import com.uade.tpejemplo.dto.response.DashboardResumenResponse;

import java.util.List;

public interface DashboardService {

    DashboardResumenResponse obtenerResumen();

    List<CreditosPorEstadoResponse> obtenerCreditosPorEstado();
}