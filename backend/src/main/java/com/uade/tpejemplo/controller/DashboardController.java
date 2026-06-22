package com.uade.tpejemplo.controller;

import com.uade.tpejemplo.dto.response.CreditosPorEstadoResponse;
import com.uade.tpejemplo.dto.response.DashboardResumenResponse;
import com.uade.tpejemplo.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumen")
    public ResponseEntity<DashboardResumenResponse> obtenerResumen() {
        return ResponseEntity.ok(dashboardService.obtenerResumen());
    }

    @GetMapping("/creditos-por-estado")
    public ResponseEntity<List<CreditosPorEstadoResponse>> obtenerCreditosPorEstado() {
        return ResponseEntity.ok(dashboardService.obtenerCreditosPorEstado());
    }
}