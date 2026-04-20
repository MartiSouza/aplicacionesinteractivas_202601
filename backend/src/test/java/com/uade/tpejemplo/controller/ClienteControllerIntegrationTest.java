package com.uade.tpejemplo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.tpejemplo.dto.request.ClienteRequest;
import com.uade.tpejemplo.model.Cliente;
import com.uade.tpejemplo.model.Credito;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClienteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private String authHeader;

    @BeforeEach
    void setUp() {
        cobranzaRepository.deleteAll();
        cuotaRepository.deleteAll();
        creditoRepository.deleteAll();
        clienteRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario usuario = usuarioRepository.saveAndFlush(Usuario.builder()
            .username("tester.clientes")
            .password("sin-uso-en-este-test")
            .rol(Rol.USER)
            .build());

        authHeader = "Bearer " + jwtUtil.generarToken(usuario);
    }

    @Test
    void actualizarCliente_devuelve200YModificaLosDatos() throws Exception {
        clienteRepository.saveAndFlush(new Cliente("12345678", "Nombre Original", null));

        ClienteRequest request = new ClienteRequest();
        request.setDni("12345678");
        request.setNombre("Nombre Actualizado");

        mockMvc.perform(put("/api/clientes/{dni}", "12345678")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dni").value("12345678"))
            .andExpect(jsonPath("$.nombre").value("Nombre Actualizado"));

        Cliente clienteActualizado = clienteRepository.findByDni("12345678").orElseThrow();
        assertThat(clienteActualizado.getNombre()).isEqualTo("Nombre Actualizado");
    }

    @Test
    void actualizarCliente_devuelve404CuandoNoExiste() throws Exception {
        ClienteRequest request = new ClienteRequest();
        request.setDni("99999999");
        request.setNombre("Cliente Inexistente");

        mockMvc.perform(put("/api/clientes/{dni}", "99999999")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("No encontrado"))
            .andExpect(jsonPath("$.mensajes[0]").value("Cliente no encontrado con DNI: '99999999'"));
    }

    @Test
    void eliminarCliente_devuelve204YEliminaSiNoTieneCreditos() throws Exception {
        clienteRepository.saveAndFlush(new Cliente("22333444", "Cliente Sin Creditos", null));

        mockMvc.perform(delete("/api/clientes/{dni}", "22333444")
                .header("Authorization", authHeader))
            .andExpect(status().isNoContent());

        assertThat(clienteRepository.findByDni("22333444")).isEmpty();
    }

    @Test
    void eliminarCliente_devuelve400YNoEliminaSiTieneCreditos() throws Exception {
        Cliente cliente = clienteRepository.saveAndFlush(new Cliente("33444555", "Cliente Con Credito", null));
        creditoRepository.saveAndFlush(new Credito(
            null,
            cliente,
            new BigDecimal("150000.00"),
            LocalDate.now(),
            new BigDecimal("50000.00"),
            3,
            null
        ));

        mockMvc.perform(delete("/api/clientes/{dni}", "33444555")
                .header("Authorization", authHeader))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Error de negocio"))
            .andExpect(jsonPath("$.mensajes[0]").value("No se puede eliminar el cliente porque tiene creditos asociados"));

        assertThat(clienteRepository.findByDni("33444555")).isPresent();
        assertThat(creditoRepository.findByClienteDni("33444555")).hasSize(1);
    }

    @Test
    void eliminarCliente_devuelve404CuandoNoExiste() throws Exception {
        mockMvc.perform(delete("/api/clientes/{dni}", "55666777")
                .header("Authorization", authHeader))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("No encontrado"))
            .andExpect(jsonPath("$.mensajes[0]").value("Cliente no encontrado con DNI: '55666777'"));

        assertThat(clienteRepository.findByDni("55666777")).isEmpty();
    }
}