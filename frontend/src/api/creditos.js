import { api } from './apiClient';

export const getCreditosPorCliente = (dni) => api.get(`/creditos/cliente/${dni}`);
export const getCredito            = (id)  => api.get(`/creditos/${id}`);
export const crearCredito          = (data) => api.post('/creditos', data);
export const anularCredito         = (id) => api.delete(`/creditos/${id}`);
