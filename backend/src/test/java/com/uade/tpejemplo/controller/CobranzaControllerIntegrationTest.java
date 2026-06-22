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
class CobranzaControllerIntegrationTest {

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
    void anularCobranza_devuelve200YMarcaAnuladaCuandoEsDelDiaYUsuarioTienePermiso() throws Exception {
        String authHeader = crearAuthHeader(false, true, "tester.cobranza.ok");
        Cobranza cobranza = crearCobranza(LocalDate.now());

        mockMvc.perform(delete("/api/cobranzas/{id}", cobranza.getId())
                .header("Authorization", authHeader))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(cobranza.getId()))
            .andExpect(jsonPath("$.anulada").value(true));

        assertThat(cobranzaRepository.findById(cobranza.getId()).orElseThrow().isAnulada()).isTrue();
    }

    @Test
    void anularCobranza_devuelve400SiNoEsDelDia() throws Exception {
        String authHeader = crearAuthHeader(false, true, "tester.cobranza.fecha");
        Cobranza cobranza = crearCobranza(LocalDate.now().minusDays(1));

        mockMvc.perform(delete("/api/cobranzas/{id}", cobranza.getId())
                .header("Authorization", authHeader))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Error de negocio"))
            .andExpect(jsonPath("$.mensajes[0]").value("Solo se pueden anular cobranzas del día de hoy."));

        assertThat(cobranzaRepository.findById(cobranza.getId()).orElseThrow().isAnulada()).isFalse();
    }

    @Test
    void anularCobranza_devuelve403SiUsuarioNoTienePermiso() throws Exception {
        String authHeader = crearAuthHeader(false, false, "tester.cobranza.denegado");
        Cobranza cobranza = crearCobranza(LocalDate.now());

        mockMvc.perform(delete("/api/cobranzas/{id}", cobranza.getId())
                .header("Authorization", authHeader))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.error").value("Acceso denegado"))
            .andExpect(jsonPath("$.mensajes[0]").value("No tiene permiso para anular cobranzas."));

        assertThat(cobranzaRepository.findById(cobranza.getId()).orElseThrow().isAnulada()).isFalse();
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

    private Cobranza crearCobranza(LocalDate fechaCobranza) {
        Cliente cliente = clienteRepository.saveAndFlush(new Cliente("50111222", "Cliente Cobranza", null));
        Credito credito = creditoRepository.saveAndFlush(new Credito(
            null,
            cliente,
            new BigDecimal("80000.00"),
            LocalDate.now().minusMonths(1),
            new BigDecimal("40000.00"),
            2,
            null
        ));

        Cuota cuota = cuotaRepository.saveAndFlush(new Cuota(
            new CuotaId(credito.getId(), 1),
            credito,
            LocalDate.now().plusDays(15)
        ));

        return cobranzaRepository.saveAndFlush(new Cobranza(
            null,
            cuota,
            new BigDecimal("40000.00"),
            fechaCobranza,
            false
        ));
    }
}