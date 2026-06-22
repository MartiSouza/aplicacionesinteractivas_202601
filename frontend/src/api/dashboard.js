import { api } from './apiClient';

export const getDashboardResumen = () => api.get('/dashboard/resumen');
export const getDashboardCreditosPorEstado = () => api.get('/dashboard/creditos-por-estado');