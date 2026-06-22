package com.uade.tpejemplo.controller;

import com.uade.tpejemplo.model.Cliente;
import com.uade.tpejemplo.model.Cobranza;
import com.uade.tpejemplo.model.Credito;
import com.uade.tpejemplo.model.Cuota;
import com.uade.tpejemplo.model.CuotaId;
import com.uade.tpejemplo.model.Rol;
import com.uade.tpejemplo.model.Usuario;
import com.uade.tpejemplo.repository.ClienteRepository;
import com.uade.tpejemplo.repository.CobranzaRepository;
import com.uade.tpejemplo.repository.CreditoRepository;
import com.uade.tpejemplo.repository.CuotaRepository;
import com.uade.tpejemplo.repository.UsuarioRepository;
import com.uade.tpejemplo.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CreditoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CreditoRepository creditoRepository;

    @Autowired
    private CuotaRepository cuotaRepository;

    @Autowired
    private CobranzaRepository cobranzaRepository;

    @BeforeEach
    void setUp() {
        cobranzaRepository.deleteAll();
        cuotaRepository.deleteAll();
        creditoRepository.deleteAll();
        clienteRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void anularCredito_devuelve200YMarcaAnuladoCuandoUsuarioTienePermisoYSinCobranzas() throws Exception {
        String authHeader = crearAuthHeader(true, false, "tester.credito.ok");
        Credito credito = crearCreditoConCuota("40111222", "Cliente Sin Cobranza");

        mockMvc.perform(delete("/api/creditos/{id}", credito.getId())
                .header("Authorization", authHeader))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(credito.getId()))
            .andExpect(jsonPath("$.anulado").value(true));

        assertThat(creditoRepository.findById(credito.getId())).isPresent();
        assertThat(creditoRepository.findById(credito.getId()).orElseThrow().isAnulado()).isTrue();
    }

    @Test
    void anularCredito_devuelve400SiTieneCobranzasRegistradas() throws Exception {
        String authHeader = crearAuthHeader(true, false, "tester.credito.cobranza");
        Credito credito = crearCreditoConCuota("40222333", "Cliente Con Cobranza");
        Cuota cuota = cuotaRepository.findById(new CuotaId(credito.getId(), 1)).orElseThrow();
        cobranzaRepository.saveAndFlush(new Cobranza(null, cuota, new BigDecimal("50000.00")));

        mockMvc.perform(delete("/api/creditos/{id}", credito.getId())
                .header("Authorization", authHeader))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Error de negocio"))
            .andExpect(jsonPath("$.mensajes[0]").value(
                "No se puede anular el crédito " + credito.getId() + " porque tiene cobranzas registradas."
            ));

        assertThat(creditoRepository.findById(credito.getId()).orElseThrow().isAnulado()).isFalse();
    }

    @Test
    void anularCredito_devuelve403SiUsuarioNoTienePermisos() throws Exception {
        String authHeader = crearAuthHeader(false, false, "tester.credito.denegado");
        Credito credito = crearCreditoConCuota("40333444", "Cliente Sin Permiso");

        mockMvc.perform(delete("/api/creditos/{id}", credito.getId())
                .header("Authorization", authHeader))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.error").value("Acceso denegado"))
            .andExpect(jsonPath("$.mensajes[0]").value("No tiene permisos para anular créditos."));

        assertThat(creditoRepository.findById(credito.getId()).orElseThrow().isAnulado()).isFalse();
    }

    private String crearAuthHeader(boolean puedeAnularCredito, boolean puedeAnularCobranza, String username) {
        Usuario usuario = usuarioRepository.saveAndFlush(Usuario.builder()
            .username(username)
            .password("sin-uso-en-este-test")
            .rol(Rol.USER)
            .puedeAnularCredito(puedeAnularCredito)
            .puedeAnularCobranza(puedeAnularCobranza)
            .build());

        return "Bearer " + jwtUtil.generarToken(usuario);
    }

    private Credito crearCreditoConCuota(String dni, String nombreCliente) {
        Cliente cliente = clienteRepository.saveAndFlush(new Cliente(dni, nombreCliente, null));
        Credito credito = creditoRepository.saveAndFlush(new Credito(
            null,
            cliente,
            new BigDecimal("100000.00"),
            LocalDate.now(),
            new BigDecimal("50000.00"),
            2,
            null
        ));

        cuotaRepository.saveAndFlush(new Cuota(
            new CuotaId(credito.getId(), 1),
            credito,
            LocalDate.now().plusMonths(1)
        ));

        return credito;
    }
}