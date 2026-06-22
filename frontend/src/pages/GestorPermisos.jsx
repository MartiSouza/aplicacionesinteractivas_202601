import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { clearError, fetchUsuariosPermisos, updatePermisosUsuario } from '../store/slices/permisosSlice';

export default function GestorPermisos() {
  const dispatch = useDispatch();
  const { usuarios, loading, error, updatingUserId } = useSelector((state) => state.permisos);
  // CAMBIO: aunque el endpoint devuelve usuarios gestionables, la vista trabaja solo con filas USER.
  const usuariosUser = usuarios.filter((usuario) => usuario.rol === 'USER');

  useEffect(() => {
    dispatch(fetchUsuariosPermisos());
  }, [dispatch]);

  // CAMBIO: cada toggle persiste inmediatamente el permiso modificado del usuario.
  const handleToggle = (usuario, campo) => {
    dispatch(clearError());
    dispatch(updatePermisosUsuario({
      id: usuario.id,
      permisos: {
        puedeAnularCredito: campo === 'puedeAnularCredito' ? !usuario.puedeAnularCredito : usuario.puedeAnularCredito,
        puedeAnularCobranza: campo === 'puedeAnularCobranza' ? !usuario.puedeAnularCobranza : usuario.puedeAnularCobranza,
      },
    }));
  };

  return (
    <div style={styles.page}>
      <h2 style={styles.title}>Gestor de permisos</h2>

      {error && <div style={styles.error}>{error}</div>}

      <div style={styles.card}>
        {loading ? (
          <p style={styles.empty}>Cargando usuarios...</p>
        ) : usuariosUser.length === 0 ? (
          <p style={styles.empty}>No hay usuarios USER para administrar.</p>
        ) : (
          <table style={styles.table}>
            <thead>
              <tr>
                <th>Username</th>
                <th>Rol</th>
                <th>Anular crédito</th>
                <th>Anular cobranza</th>
              </tr>
            </thead>
            <tbody>
              {usuariosUser.map((usuario) => {
                const disabled = updatingUserId === usuario.id;

                return (
                  <tr key={usuario.id}>
                    <td>{usuario.username}</td>
                    <td>{usuario.rol}</td>
                    <td>
                      <input
                        type="checkbox"
                        checked={usuario.puedeAnularCredito}
                        onChange={() => handleToggle(usuario, 'puedeAnularCredito')}
                        disabled={disabled}
                      />
                    </td>
                    <td>
                      <input
                        type="checkbox"
                        checked={usuario.puedeAnularCobranza}
                        onChange={() => handleToggle(usuario, 'puedeAnularCobranza')}
                        disabled={disabled}
                      />
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

const styles = {
  page: { padding:'32px', maxWidth:'900px', margin:'0 auto' },
  title: { color:'#1e3a5f', marginBottom:'24px' },
  card: { background:'white', padding:'24px', borderRadius:'12px', boxShadow:'0 2px 10px rgba(0,0,0,0.08)' },
  error: { background:'#ffebee', color:'#c62828', padding:'10px', borderRadius:'6px', marginBottom:'12px', fontSize:'0.9rem' },
  empty: { color:'#999' },
  table: { width:'100%', borderCollapse:'collapse' },
};