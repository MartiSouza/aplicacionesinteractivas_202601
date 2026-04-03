package com.uade.tpejemplo.service.impl;

import com.uade.tpejemplo.dto.response.CreditosPorEstadoResponse;
import com.uade.tpejemplo.dto.response.DashboardResumenResponse;
import com.uade.tpejemplo.model.Cobranza;
import com.uade.tpejemplo.model.Credito;
import com.uade.tpejemplo.model.Cuota;
import com.uade.tpejemplo.repository.CobranzaRepository;
import com.uade.tpejemplo.repository.CreditoRepository;
import com.uade.tpejemplo.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final String ESTADO_ACTIVO = "ACTIVO";
    private static final String ESTADO_EN_MORA = "EN_MORA";
    private static final String ESTADO_FINALIZADO = "FINALIZADO";

    private final CreditoRepository creditoRepository;
    private final CobranzaRepository cobranzaRepository;

    @Override
    public DashboardResumenResponse obtenerResumen() {
        long totalCreditos = creditoRepository.count();
        BigDecimal montoTotalPrestado = defaultZero(creditoRepository.sumDeudaOriginal());
        BigDecimal montoTotalCobrado = defaultZero(cobranzaRepository.sumImporteCobrado());

        if (totalCreditos == 0) {
            return new DashboardResumenResponse(
                0L,
                montoTotalPrestado,
                montoTotalCobrado,
                BigDecimal.ZERO,
                0L,
                0L,
                List.of()
            );
        }

        BigDecimal porcentajeRecupero = calcularPorcentajeRecupero(montoTotalCobrado, montoTotalPrestado);

        List<Credito> creditos = creditoRepository.findAllWithCuotas();
        Set<String> cuotasPagadas = cobranzaRepository.findAllWithCuota().stream()
            .map(this::toCuotaKey)
            .collect(Collectors.toSet());

        Map<String, Long> creditosPorEstado = inicializarConteoEstados();
        for (Credito credito : creditos) {
            String estado = determinarEstadoCredito(credito, cuotasPagadas, LocalDate.now());
            creditosPorEstado.computeIfPresent(estado, (key, value) -> value + 1);
        }

        return new DashboardResumenResponse(
            totalCreditos,
            montoTotalPrestado,
            montoTotalCobrado,
            porcentajeRecupero,
            creditosPorEstado.get(ESTADO_ACTIVO),
            creditosPorEstado.get(ESTADO_EN_MORA),
            toEstadoResponseList(creditosPorEstado)
        );
    }

    private BigDecimal calcularPorcentajeRecupero(BigDecimal montoTotalCobrado, BigDecimal montoTotalPrestado) {
        if (montoTotalPrestado.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return montoTotalCobrado
            .multiply(BigDecimal.valueOf(100))
            .divide(montoTotalPrestado, 2, RoundingMode.HALF_UP);
    }

    private String determinarEstadoCredito(Credito credito, Set<String> cuotasPagadas, LocalDate fechaActual) {
        List<Cuota> cuotas = credito.getCuotas();
        if (cuotas == null || cuotas.isEmpty()) {
            return ESTADO_ACTIVO;
        }

        boolean todasPagadas = true;
        boolean tieneCuotaVencidaImpaga = false;

        for (Cuota cuota : cuotas) {
            boolean pagada = cuotasPagadas.contains(toCuotaKey(cuota.getId().getIdCredito(), cuota.getId().getIdCuota()));
            if (!pagada) {
                todasPagadas = false;
                if (cuota.getFechaVencimiento() != null && cuota.getFechaVencimiento().isBefore(fechaActual)) {
                    tieneCuotaVencidaImpaga = true;
                }
            }
        }

        if (todasPagadas) {
            return ESTADO_FINALIZADO;
        }
        if (tieneCuotaVencidaImpaga) {
            return ESTADO_EN_MORA;
        }
        return ESTADO_ACTIVO;
    }

    private Map<String, Long> inicializarConteoEstados() {
        Map<String, Long> conteoEstados = new LinkedHashMap<>();
        conteoEstados.put(ESTADO_ACTIVO, 0L);
        conteoEstados.put(ESTADO_EN_MORA, 0L);
        conteoEstados.put(ESTADO_FINALIZADO, 0L);
        return conteoEstados;
    }

    private List<CreditosPorEstadoResponse> toEstadoResponseList(Map<String, Long> creditosPorEstado) {
        return creditosPorEstado.entrySet().stream()
            .map(entry -> new CreditosPorEstadoResponse(entry.getKey(), entry.getValue()))
            .toList();
    }

    private String toCuotaKey(Cobranza cobranza) {
        return toCuotaKey(cobranza.getCuota().getId().getIdCredito(), cobranza.getCuota().getId().getIdCuota());
    }

    private String toCuotaKey(Long idCredito, Integer idCuota) {
        return idCredito + "-" + idCuota;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}