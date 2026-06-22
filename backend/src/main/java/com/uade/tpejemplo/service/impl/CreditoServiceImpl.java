package com.uade.tpejemplo.service.impl;

import com.uade.tpejemplo.dto.request.CreditoRequest;
import com.uade.tpejemplo.dto.response.CreditoResponse;
import com.uade.tpejemplo.dto.response.CuotaResponse;
import com.uade.tpejemplo.exception.BusinessException;
import com.uade.tpejemplo.exception.ResourceNotFoundException;
import com.uade.tpejemplo.model.Cliente;
import com.uade.tpejemplo.model.Credito;
import com.uade.tpejemplo.model.Cuota;
import com.uade.tpejemplo.model.CuotaId;
import com.uade.tpejemplo.model.Usuario;
import com.uade.tpejemplo.repository.ClienteRepository;
import com.uade.tpejemplo.repository.CobranzaRepository;
import com.uade.tpejemplo.repository.CreditoRepository;
import com.uade.tpejemplo.repository.CuotaRepository;
import com.uade.tpejemplo.repository.UsuarioRepository;
import com.uade.tpejemplo.service.CreditoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditoServiceImpl implements CreditoService {

    private final CreditoRepository creditoRepository;
    private final ClienteRepository clienteRepository;
    private final CuotaRepository cuotaRepository;
    private final CobranzaRepository cobranzaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public CreditoResponse crear(CreditoRequest request) {
        Cliente cliente = clienteRepository.findByDni(request.getDniCliente())
            .orElseThrow(() -> new ResourceNotFoundException("Cliente", "DNI", request.getDniCliente()));

        Credito credito = new Credito(
            null,
            cliente,
            request.getDeudaOriginal(),
            request.getFecha(),
            request.getImporteCuota(),
            request.getCantidadCuotas(),
            false,
            null
        );
        creditoRepository.save(credito);

        // CAMBIO: las cuotas se siguen generando automáticamente al crear el crédito.
        List<Cuota> cuotas = new ArrayList<>();
        for (int i = 1; i <= request.getCantidadCuotas(); i++) {
            Cuota cuota = new Cuota(
                new CuotaId(credito.getId(), i),
                credito,
                request.getFecha().plusMonths(i)
            );
            cuotas.add(cuota);
        }
        cuotaRepository.saveAll(cuotas);

        return toResponse(credito, cuotas);
    }

    @Override
    public CreditoResponse buscarPorId(Long id) {
        Credito credito = creditoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Crédito", "id", id));
        List<Cuota> cuotas = cuotaRepository.findByIdIdCredito(id);
        return toResponse(credito, cuotas);
    }

    @Override
    public List<CreditoResponse> listarPorCliente(String dniCliente) {
        if (!clienteRepository.existsByDni(dniCliente)) {
            throw new ResourceNotFoundException("Cliente", "DNI", dniCliente);
        }
        return creditoRepository.findByClienteDni(dniCliente).stream()
            .map(c -> toResponse(c, cuotaRepository.findByIdIdCredito(c.getId())))
            .toList();
    }

    @Override
    @Transactional
    // CAMBIO: anulación lógica con control de permiso y rechazo si existen cobranzas registradas.
    public CreditoResponse anular(Long id) {
        Usuario usuario = getUsuarioAutenticado();
        if (!usuario.isPuedeAnularCredito()) {
            throw new AccessDeniedException("No tiene permisos para anular créditos.");
        }

        Credito credito = creditoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Crédito", "id", id));

        if (credito.isAnulado()) {
            throw new BusinessException("El crédito ya se encuentra anulado.");
        }

        if (cobranzaRepository.existsByCuotaCreditoId(id)) {
            throw new BusinessException("No se puede anular el crédito " + id + " porque tiene cobranzas registradas.");
        }

        credito.setAnulado(true);
        creditoRepository.save(credito);

        return toResponse(credito, cuotaRepository.findByIdIdCredito(id));
    }

    // CAMBIO: el estado pagada de cada cuota ahora ignora cobranzas anuladas.
    private CreditoResponse toResponse(Credito credito, List<Cuota> cuotas) {
        List<CuotaResponse> cuotasResponse = cuotas.stream()
            .map(c -> new CuotaResponse(
                c.getId().getIdCredito(),
                c.getId().getIdCuota(),
                c.getFechaVencimiento(),
                cobranzaRepository.existsByCuotaIdIdCreditoAndCuotaIdIdCuotaAndAnuladaFalse(
                    c.getId().getIdCredito(), c.getId().getIdCuota()
                )
            ))
            .toList();

        return new CreditoResponse(
            credito.getId(),
            credito.getCliente().getDni(),
            credito.getCliente().getNombre(),
            credito.getDeudaOriginal(),
            credito.getFecha(),
            credito.getImporteCuota(),
            credito.getCantidadCuotas(),
            credito.isAnulado(),
            cuotasResponse
        );
    }

    // CAMBIO: se consulta el usuario autenticado para validar permisos granulares de anulación.
    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Usuario no autenticado.");
        }

        return usuarioRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new AccessDeniedException("Usuario autenticado inválido."));
    }
}
