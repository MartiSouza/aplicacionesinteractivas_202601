package com.uade.tpejemplo.service.impl;

import com.uade.tpejemplo.dto.request.CobranzaRequest;
import com.uade.tpejemplo.dto.response.CobranzaResponse;
import com.uade.tpejemplo.exception.BusinessException;
import com.uade.tpejemplo.exception.ResourceNotFoundException;
import com.uade.tpejemplo.model.Cobranza;
import com.uade.tpejemplo.model.Cuota;
import com.uade.tpejemplo.model.CuotaId;
import com.uade.tpejemplo.model.Usuario;
import com.uade.tpejemplo.repository.CobranzaRepository;
import com.uade.tpejemplo.repository.CuotaRepository;
import com.uade.tpejemplo.repository.UsuarioRepository;
import com.uade.tpejemplo.service.CobranzaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CobranzaServiceImpl implements CobranzaService {

    private final CobranzaRepository cobranzaRepository;
    private final CuotaRepository cuotaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    // CAMBIO: se impide registrar cobranzas sobre créditos anulados y se ignoran cobranzas ya anuladas al validar pago único.
    public CobranzaResponse registrar(CobranzaRequest request) {
        CuotaId cuotaId = new CuotaId(request.getIdCredito(), request.getIdCuota());

        Cuota cuota = cuotaRepository.findById(cuotaId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cuota", "idCredito/idCuota", request.getIdCredito() + "/" + request.getIdCuota()
            ));

        if (cuota.getCredito().isAnulado()) {
            throw new BusinessException("No se pueden registrar cobranzas para un crédito anulado.");
        }

        if (cobranzaRepository.existsByCuotaIdIdCreditoAndCuotaIdIdCuotaAndAnuladaFalse(request.getIdCredito(), request.getIdCuota())) {
            throw new BusinessException(
                "La cuota " + request.getIdCuota() + " del crédito " + request.getIdCredito() + " ya fue pagada"
            );
        }

        Cobranza cobranza = new Cobranza(null, cuota, request.getImporte(), LocalDate.now(), false);
        cobranzaRepository.save(cobranza);
        return toResponse(cobranza);
    }

    @Override
    public List<CobranzaResponse> listarPorCredito(Long idCredito) {
        return cobranzaRepository.findByCuotaIdIdCredito(idCredito).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional
    // CAMBIO: anulación lógica restringida por permiso y fecha de cobranza del día actual.
    public CobranzaResponse anular(Long id) {
        Usuario usuario = getUsuarioAutenticado();
        if (!usuario.isPuedeAnularCobranza()) {
            throw new AccessDeniedException("No tiene permiso para anular cobranzas.");
        }

        Cobranza cobranza = cobranzaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cobranza", "id", id));

        if (cobranza.isAnulada()) {
            throw new BusinessException("La cobranza ya se encuentra anulada.");
        }

        if (!LocalDate.now().equals(cobranza.getFechaCobranza())) {
            throw new BusinessException("Solo se pueden anular cobranzas del día de hoy.");
        }

        cobranza.setAnulada(true);
        cobranzaRepository.save(cobranza);
        return toResponse(cobranza);
    }

    // CAMBIO: la respuesta expone fecha y estado de anulación para que el frontend pueda diferenciar cobranzas anuladas.
    private CobranzaResponse toResponse(Cobranza cobranza) {
        return new CobranzaResponse(
            cobranza.getId(),
            cobranza.getCuota().getId().getIdCredito(),
            cobranza.getCuota().getId().getIdCuota(),
            cobranza.getImporte(),
            cobranza.getFechaCobranza(),
            cobranza.isAnulada()
        );
    }

    // CAMBIO: se reutiliza el usuario autenticado para aplicar permisos específicos de cobranza.
    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Usuario no autenticado.");
        }

        return usuarioRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new AccessDeniedException("Usuario autenticado inválido."));
    }
}
