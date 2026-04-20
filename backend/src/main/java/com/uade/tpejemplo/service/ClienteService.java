package com.uade.tpejemplo.service;

import com.uade.tpejemplo.dto.request.ClienteRequest;
import com.uade.tpejemplo.dto.response.ClienteResponse;

import java.util.List;

public interface ClienteService {

    ClienteResponse crear(ClienteRequest request);

    ClienteResponse buscarPorDni(String dni);

    List<ClienteResponse> listarTodos();

    ClienteResponse actualizar(String dni, ClienteRequest request);

    void eliminar(String dni);
}
