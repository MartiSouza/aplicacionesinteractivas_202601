package com.uade.tpejemplo.service.impl;

import com.uade.tpejemplo.dto.response.CreditosPorEstadoResponse;
import com.uade.tpejemplo.dto.response.DashboardResumenResponse;
import com.uade.tpejemplo.model.Cliente;
import com.uade.tpejemplo.model.Cobranza;
import com.uade.tpejemplo.model.Credito;
import com.uade.tpejemplo.model.Cuota;
import com.uade.tpejemplo.model.CuotaId;
import com.uade.tpejemplo.repository.ClienteRepository;
import com.uade.tpejemplo.repository.CobranzaRepository;
import com.uade.tpejemplo.repository.CreditoRepository;
import com.uade.tpejemplo.repository.CuotaRepository;
import com.uade.tpejemplo.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DashboardServiceImplIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CreditoRepository creditoRepository;

    @Autowired
    private CuotaRepository cuotaRepository;

    @Autowired
    private CobranzaRepository cobranzaRepository;

    @BeforeEach
    void limpiarBase() {
        cobranzaRepository.deleteAll();
        cuotaRepository.deleteAll();
        creditoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    void obtenerResumen_devuelveCerosConBaseVacia() {
        DashboardResumenResponse resumen = dashboardService.obtenerResumen();

        assertThat(resumen.getTotalCreditos()).isEqualTo(0L);
        assertThat(resumen.getMontoTotalPrestado()).isEqualByComparingTo("0");
        assertThat(resumen.getMontoTotalCobrado()).isEqualByComparingTo("0");
        assertThat(resumen.getPorcentajeRecupero()).isEqualByComparingTo("0");
        assertThat(resumen.getCantidadCreditosActivos()).isEqualTo(0L);
        assertThat(resumen.getCantidadCreditosEnMora()).isEqualTo(0L);
        assertThat(resumen.getCreditosPorEstado()).isEmpty();
    }

    @Test
    void obtenerResumen_devuelveActivoSinCobranzas() {
        Credito credito = crearCreditoConCuotas(
            "100",
            "Cliente Activo Sin Cobranza",
            new BigDecimal("1000.00"),
            LocalDate.now(),
            new BigDecimal("500.00"),
            List.of(LocalDate.now().plusDays(10), LocalDate.now().plusDays(40))
        );

        DashboardResumenResponse resumen = dashboardService.obtenerResumen();
        Map<String, Long> estados = toEstadoMap(resumen);

        assertThat(credito.getId()).isNotNull();
        assertThat(resumen.getTotalCreditos()).isEqualTo(1L);
        assertThat(resumen.getMontoTotalPrestado()).isEqualByComparingTo("1000.00");
        assertThat(resumen.getMontoTotalCobrado()).isEqualByComparingTo("0");
        assertThat(resumen.getPorcentajeRecupero()).isEqualByComparingTo("0");
        assertThat(resumen.getCantidadCreditosActivos()).isEqualTo(1L);
        assertThat(resumen.getCantidadCreditosEnMora()).isEqualTo(0L);
        assertThat(estados.get("ACTIVO")).isEqualTo(1L);
        assertThat(estados.get("EN_MORA")).isEqualTo(0L);
        assertThat(estados.get("FINALIZADO")).isEqualTo(0L);
    }

    @Test
    void obtenerResumen_detectaCreditoFinalizado() {
        Credito credito = crearCreditoConCuotas(
            "200",
            "Cliente Finalizado",
            new BigDecimal("1000.00"),
            LocalDate.now().minusMonths(2),
            new BigDecimal("500.00"),
            List.of(LocalDate.now().minusMonths(1), LocalDate.now().plusDays(5))
        );
        registrarCobranza(credito.getId(), 1, new BigDecimal("500.00"));
        registrarCobranza(credito.getId(), 2, new BigDecimal("500.00"));

        DashboardResumenResponse resumen = dashboardService.obtenerResumen();
        Map<String, Long> estados = toEstadoMap(resumen);

        assertThat(resumen.getTotalCreditos()).isEqualTo(1L);
        assertThat(resumen.getMontoTotalPrestado()).isEqualByComparingTo("1000.00");
        assertThat(resumen.getMontoTotalCobrado()).isEqualByComparingTo("1000.00");
        assertThat(resumen.getPorcentajeRecupero()).isEqualByComparingTo("100.00");
        assertThat(resumen.getCantidadCreditosActivos()).isEqualTo(0L);
        assertThat(resumen.getCantidadCreditosEnMora()).isEqualTo(0L);
        assertThat(estados.get("ACTIVO")).isEqualTo(0L);
        assertThat(estados.get("EN_MORA")).isEqualTo(0L);
        assertThat(estados.get("FINALIZADO")).isEqualTo(1L);
    }

    @Test
    void obtenerResumen_detectaCreditoEnMora() {
        crearCreditoConCuotas(
            "300",
            "Cliente En Mora",
            new BigDecimal("1200.00"),
            LocalDate.now().minusMonths(3),
            new BigDecimal("600.00"),
            List.of(LocalDate.now().minusDays(20), LocalDate.now().plusDays(20))
        );

        DashboardResumenResponse resumen = dashboardService.obtenerResumen();
        Map<String, Long> estados = toEstadoMap(resumen);

        assertThat(resumen.getTotalCreditos()).isEqualTo(1L);
        assertThat(resumen.getMontoTotalPrestado()).isEqualByComparingTo("1200.00");
        assertThat(resumen.getMontoTotalCobrado()).isEqualByComparingTo("0");
        assertThat(resumen.getPorcentajeRecupero()).isEqualByComparingTo("0");
        assertThat(resumen.getCantidadCreditosActivos()).isEqualTo(0L);
        assertThat(resumen.getCantidadCreditosEnMora()).isEqualTo(1L);
        assertThat(estados.get("ACTIVO")).isEqualTo(0L);
        assertThat(estados.get("EN_MORA")).isEqualTo(1L);
        assertThat(estados.get("FINALIZADO")).isEqualTo(0L);
    }

    @Test
    void obtenerResumen_detectaCreditoActivo() {
        crearCreditoConCuotas(
            "400",
            "Cliente Activo",
            new BigDecimal("900.00"),
            LocalDate.now(),
            new BigDecimal("300.00"),
            List.of(LocalDate.now().plusDays(15), LocalDate.now().plusDays(45), LocalDate.now().plusDays(75))
        );

        DashboardResumenResponse resumen = dashboardService.obtenerResumen();
        Map<String, Long> estados = toEstadoMap(resumen);

        assertThat(resumen.getTotalCreditos()).isEqualTo(1L);
        assertThat(resumen.getCantidadCreditosActivos()).isEqualTo(1L);
        assertThat(resumen.getCantidadCreditosEnMora()).isEqualTo(0L);
        assertThat(estados.get("ACTIVO")).isEqualTo(1L);
        assertThat(estados.get("EN_MORA")).isEqualTo(0L);
        assertThat(estados.get("FINALIZADO")).isEqualTo(0L);
    }

    @Test
    void obtenerResumen_calculaPorcentajeRecuperoConMultiplesCreditos() {
        Credito creditoFinalizado = crearCreditoConCuotas(
            "500",
            "Cliente Recupero Uno",
            new BigDecimal("1000.00"),
            LocalDate.now().minusMonths(2),
            new BigDecimal("500.00"),
            List.of(LocalDate.now().minusMonths(1), LocalDate.now().plusDays(5))
        );
        registrarCobranza(creditoFinalizado.getId(), 1, new BigDecimal("500.00"));
        registrarCobranza(creditoFinalizado.getId(), 2, new BigDecimal("500.00"));

        Credito creditoActivo = crearCreditoConCuotas(
            "501",
            "Cliente Recupero Dos",
            new BigDecimal("500.00"),
            LocalDate.now(),
            new BigDecimal("250.00"),
            List.of(LocalDate.now().plusDays(20), LocalDate.now().plusDays(50))
        );
        registrarCobranza(creditoActivo.getId(), 1, new BigDecimal("250.00"));

        DashboardResumenResponse resumen = dashboardService.obtenerResumen();
        Map<String, Long> estados = toEstadoMap(resumen);

        assertThat(resumen.getTotalCreditos()).isEqualTo(2L);
        assertThat(resumen.getMontoTotalPrestado()).isEqualByComparingTo("1500.00");
        assertThat(resumen.getMontoTotalCobrado()).isEqualByComparingTo("1250.00");
        assertThat(resumen.getPorcentajeRecupero()).isEqualByComparingTo("83.33");
        assertThat(resumen.getCantidadCreditosActivos()).isEqualTo(1L);
        assertThat(resumen.getCantidadCreditosEnMora()).isEqualTo(0L);
        assertThat(estados.get("ACTIVO")).isEqualTo(1L);
        assertThat(estados.get("EN_MORA")).isEqualTo(0L);
        assertThat(estados.get("FINALIZADO")).isEqualTo(1L);
    }

    private Credito crearCreditoConCuotas(String dni,
                                          String nombre,
                                          BigDecimal deudaOriginal,
                                          LocalDate fechaCredito,
                                          BigDecimal importeCuota,
                                          List<LocalDate> fechasVencimiento) {
        Cliente cliente = clienteRepository.saveAndFlush(new Cliente(dni, nombre, null));

        Credito credito = creditoRepository.saveAndFlush(new Credito(
            null,
            cliente,
            deudaOriginal,
            fechaCredito,
            importeCuota,
            fechasVencimiento.size(),
            null
        ));

        List<Cuota> cuotas = fechasVencimiento.stream()
            .collect(Collectors.collectingAndThen(Collectors.toList(), fechas -> {
                List<Cuota> nuevasCuotas = new ArrayList<>();
                for (int index = 0; index < fechas.size(); index++) {
                    nuevasCuotas.add(new Cuota(
                        new CuotaId(credito.getId(), index + 1),
                        credito,
                        fechas.get(index)
                    ));
                }
                return nuevasCuotas;
            }));

        cuotaRepository.saveAllAndFlush(cuotas);
        return credito;
    }

    private void registrarCobranza(Long idCredito, Integer idCuota, BigDecimal importe) {
        Cuota cuota = cuotaRepository.findById(new CuotaId(idCredito, idCuota)).orElseThrow();
        cobranzaRepository.saveAndFlush(new Cobranza(null, cuota, importe));
    }

    private Map<String, Long> toEstadoMap(DashboardResumenResponse resumen) {
        return resumen.getCreditosPorEstado().stream()
            .collect(Collectors.toMap(CreditosPorEstadoResponse::getEstado,
                CreditosPorEstadoResponse::getCantidad,
                (left, right) -> right,
                java.util.LinkedHashMap::new));
    }
}