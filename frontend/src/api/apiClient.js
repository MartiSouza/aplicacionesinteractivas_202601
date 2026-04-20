const BASE_URL = '/api';

async function request(path, options = {}) {
  const token = localStorage.getItem('token');
  const headers = { 'Content-Type': 'application/json', ...options.headers };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${BASE_URL}${path}`, { ...options, headers });

  if (!res.ok) {
    // CAMBIO: manejar sesión expirada de forma centralizada ante respuestas 401.
    if (res.status === 401) {
      localStorage.removeItem('authUser');
      localStorage.removeItem('token');
      window.location.href = '/login';
      throw new Error('Tu sesión expiró. Iniciá sesión nuevamente.');
    }

    const error = await res.json().catch(() => ({ mensajes: [res.statusText] }));
    throw new Error(error.mensajes?.[0] ?? 'Error desconocido');
  }

  return res.status === 204 ? null : res.json();
}

export const api = {
  get:    (path)         => request(path),
  post:   (path, body)   => request(path, { method: 'POST',   body: JSON.stringify(body) }),
  put:    (path, body)   => request(path, { method: 'PUT',    body: JSON.stringify(body) }),
  delete: (path)         => request(path, { method: 'DELETE' }),
};
