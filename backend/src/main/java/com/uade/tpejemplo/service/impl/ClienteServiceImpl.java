package com.uade.tpejemplo.service.impl;

import com.uade.tpejemplo.dto.request.ClienteRequest;
import com.uade.tpejemplo.dto.response.ClienteResponse;
import com.uade.tpejemplo.exception.BusinessException;
import com.uade.tpejemplo.exception.ResourceNotFoundException;
import com.uade.tpejemplo.model.Cliente;
import com.uade.tpejemplo.repository.ClienteRepository;
import com.uade.tpejemplo.repository.CreditoRepository;
import com.uade.tpejemplo.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final CreditoRepository creditoRepository;

    @Override
    public ClienteResponse crear(ClienteRequest request) {
        if (clienteRepository.existsByDni(request.getDni())) {
            throw new BusinessException("Ya existe un cliente con DNI: " + request.getDni());
        }
        Cliente cliente = new Cliente(request.getDni(), request.getNombre(), null);
        clienteRepository.save(cliente);
        return toResponse(cliente);
    }

    @Override
    public ClienteResponse buscarPorDni(String dni) {
        Cliente cliente = clienteRepository.findByDni(dni)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente", "DNI", dni));
        return toResponse(cliente);
    }

    @Override
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public ClienteResponse actualizar(String dni, ClienteRequest request) {
        Cliente cliente = clienteRepository.findByDni(dni)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente", "DNI", dni));

        // CAMBIO: actualiza solo los datos basicos del cliente existente.
        cliente.setNombre(request.getNombre());
        clienteRepository.save(cliente);
        return toResponse(cliente);
    }

    @Override
    public void eliminar(String dni) {
        Cliente cliente = clienteRepository.findByDni(dni)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente", "DNI", dni));

        // CAMBIO: impedir eliminacion si el cliente ya tiene creditos asociados.
        if (creditoRepository.existsByClienteDni(dni)) {
            throw new BusinessException("No se puede eliminar el cliente porque tiene creditos asociados");
        }

        clienteRepository.delete(cliente);
    }

    private ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(cliente.getDni(), cliente.getNombre());
    }
}
