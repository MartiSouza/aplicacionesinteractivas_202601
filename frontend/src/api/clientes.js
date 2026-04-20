import { api } from './apiClient';

export const getClientes  = ()      => api.get('/clientes');
export const getCliente   = (dni)   => api.get(`/clientes/${dni}`);
export const crearCliente = (data)  => api.post('/clientes', data);
// CAMBIO: operaciones minimas de update y delete para clientes.
export const updateCliente = (dni, data) => api.put(`/clientes/${dni}`, data);
export const deleteCliente = (dni) => api.delete(`/clientes/${dni}`);
