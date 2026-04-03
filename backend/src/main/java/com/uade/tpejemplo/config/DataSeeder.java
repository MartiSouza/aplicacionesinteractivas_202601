package com.uade.tpejemplo.config;

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
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final CreditoRepository creditoRepository;
    private final CuotaRepository cuotaRepository;
    private final CobranzaRepository cobranzaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0 || clienteRepository.count() > 0 || creditoRepository.count() > 0) {
            return;
        }

        Usuario usuarioDemo = Usuario.builder()
            .username("martina.f")
            .password(passwordEncoder.encode("123456"))
            .rol(Rol.USER)
            .build();
        usuarioRepository.save(usuarioDemo);

        Cliente clienteActivo = clienteRepository.save(new Cliente("30111222", "Lucia Gomez", null));
        Cliente clienteMora = clienteRepository.save(new Cliente("30222333", "Carlos Diaz", null));
        Cliente clienteFinalizado = clienteRepository.save(new Cliente("30333444", "Ana Perez", null));

        Credito creditoActivo = crearCreditoConCuotas(
            clienteActivo,
            new BigDecimal("240000.00"),
            LocalDate.now().minusMonths(1),
            new BigDecimal("40000.00"),
            6
        );

        Credito creditoEnMora = crearCreditoConCuotas(
            clienteMora,
            new BigDecimal("180000.00"),
            LocalDate.now().minusMonths(5),
            new BigDecimal("30000.00"),
            6
        );

        Credito creditoFinalizado = crearCreditoConCuotas(
            clienteFinalizado,
            new BigDecimal("90000.00"),
            LocalDate.now().minusMonths(4),
            new BigDecimal("30000.00"),
            3
        );

        registrarCobranza(creditoActivo.getId(), 1, new BigDecimal("40000.00"));

        registrarCobranza(creditoFinalizado.getId(), 1, new BigDecimal("30000.00"));
        registrarCobranza(creditoFinalizado.getId(), 2, new BigDecimal("30000.00"));
        registrarCobranza(creditoFinalizado.getId(), 3, new BigDecimal("30000.00"));
    }

    private Credito crearCreditoConCuotas(Cliente cliente,
                                          BigDecimal deudaOriginal,
                                          LocalDate fecha,
                                          BigDecimal importeCuota,
                                          int cantidadCuotas) {
        Credito credito = creditoRepository.save(new Credito(
            null,
            cliente,
            deudaOriginal,
            fecha,
            importeCuota,
            cantidadCuotas,
            null
        ));

        List<Cuota> cuotas = new ArrayList<>();
        for (int i = 1; i <= cantidadCuotas; i++) {
            cuotas.add(new Cuota(
                new CuotaId(credito.getId(), i),
                credito,
                fecha.plusMonths(i)
            ));
        }
        cuotaRepository.saveAll(cuotas);

        return credito;
    }

    private void registrarCobranza(Long idCredito, int idCuota, BigDecimal importe) {
        cuotaRepository.findById(new CuotaId(idCredito, idCuota))
            .ifPresent(cuota -> cobranzaRepository.save(new Cobranza(null, cuota, importe)));
    }
}