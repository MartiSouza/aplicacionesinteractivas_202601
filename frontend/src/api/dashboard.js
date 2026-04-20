import { api } from './apiClient';

// CAMBIO: API del dashboard reutilizando el cliente HTTP existente.
export const getDashboardResumen = () => api.get('/dashboard/resumen');