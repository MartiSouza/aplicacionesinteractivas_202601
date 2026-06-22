import { api } from './apiClient';

// CAMBIO: endpoints del gestor de permisos consumidos por el slice administrativo.
export const getUsuarios = () => api.get('/admin/usuarios');

export const updatePermisos = (id, permisos) => api.put(`/admin/usuarios/${id}/permisos`, permisos);