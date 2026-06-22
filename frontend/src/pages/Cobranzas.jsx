import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchCobranzasPorCredito, addCobranza, anularCobranza, clearCobranzas, clearError } from '../store/slices/cobranzasSlice';

const formatearFecha = (fecha) => fecha ? new Date(`${fecha}T00:00:00`).toLocaleDateString('es-AR') : '-';

export default function Cobranzas() {
  const dispatch = useDispatch();
  const { lista, loading, error } = useSelector((state) => state.cobranzas);
  // CAMBIO: se consulta el permiso del usuario para decidir si mostrar la acción de anular.
  const user = useSelector((state) => state.auth.user);
  const [idCredito, setIdCredito] = useState('');
  const [buscado, setBuscado]     = useState(false);
  const [form, setForm]           = useState({ idCredito:'', idCuota:'', importe:'' });

  const buscar = async (e) => {
    e.preventDefault();
    dispatch(clearError());
    dispatch(clearCobranzas());
    const result = await dispatch(fetchCobranzasPorCredito(idCredito));
    if (result.meta.requestStatus === 'fulfilled') setBuscado(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    dispatch(clearError());
    const payload = { idCredito: Number(form.idCredito), idCuota: Number(form.idCuota), importe: Number(form.importe) };
    const result = await dispatch(addCobranza(payload));
    if (result.meta.requestStatus === 'fulfilled') {
      setForm({ idCredito:'', idCuota:'', importe:'' });
      if (String(form.idCredito) === idCredito) dispatch(fetchCobranzasPorCredito(idCredito));
    }
  };

  // CAMBIO: la anulación se ejecuta contra backend para respetar regla de fecha y permisos.
  const handleAnular = async (id) => {
    dispatch(clearError());
    await dispatch(anularCobranza(id));
  };

  return (
    <div style={styles.page}>
      <h2 style={styles.title}>Cobranzas</h2>

      {error && <div style={styles.error}>{error}</div>}

      <div style={styles.card}>
        <h3>Buscar cobranzas por crédito</h3>
        <form onSubmit={buscar} style={styles.row}>
          <input style={styles.input} placeholder="ID del crédito" type="number" value={idCredito} onChange={e => setIdCredito(e.target.value)} required />
          <button style={styles.btn}>Buscar</button>
        </form>
      </div>

      <div style={styles.card}>
        <h3>Registrar pago de cuota</h3>
        <form onSubmit={handleSubmit} style={styles.row}>
          <input style={styles.input} placeholder="ID crédito" type="number" value={form.idCredito} onChange={e => setForm({...form, idCredito: e.target.value})} required />
          <input style={styles.input} placeholder="Nro. cuota"  type="number" min="1" value={form.idCuota}   onChange={e => setForm({...form, idCuota: e.target.value})}   required />
          <input style={styles.input} placeholder="Importe"     type="number" value={form.importe}    onChange={e => setForm({...form, importe: e.target.value})}    required />
          <button style={styles.btn} disabled={loading}>{loading ? 'Registrando...' : 'Registrar'}</button>
        </form>
      </div>

      {buscado && (
        <div style={styles.card}>
          <h3>Cobranzas del crédito #{idCredito} ({lista.length})</h3>
          {loading && <p style={styles.empty}>Cargando...</p>}
          {!loading && lista.length === 0 && <p style={styles.empty}>Sin cobranzas registradas.</p>}
          {lista.length > 0 && (
            <table style={styles.table}>
              <thead><tr><th>ID</th><th>Crédito</th><th>Cuota</th><th>Fecha</th><th>Importe</th><th>Estado</th><th>Acción</th></tr></thead>
              <tbody>
                {lista.map(c => (
                  // CAMBIO: las cobranzas anuladas se renderizan diferenciadas y sin acción disponible.
                  <tr key={c.id} style={c.anulada ? styles.filaAnulada : undefined}>
                    <td>#{c.id}</td>
                    <td>{c.idCredito}</td>
                    <td>{c.idCuota}</td>
                    <td>{formatearFecha(c.fechaCobranza)}</td>
                    <td>${c.importe}</td>
                    <td>{c.anulada ? <span style={styles.badgeAnulada}>ANULADA</span> : 'Vigente'}</td>
                    <td>
                      {!c.anulada && user?.puedeAnularCobranza && (
                        <button type="button" onClick={() => handleAnular(c.id)} style={styles.secondaryBtn} disabled={loading}>
                          Anular
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}

const styles = {
  page:  { padding:'32px', maxWidth:'800px', margin:'0 auto' },
  title: { color:'#1e3a5f', marginBottom:'24px' },
  card:  { background:'white', padding:'24px', borderRadius:'12px', boxShadow:'0 2px 10px rgba(0,0,0,0.08)', marginBottom:'24px' },
  row:   { display:'flex', gap:'12px', flexWrap:'wrap' },
  input: { padding:'10px', border:'1px solid #ccc', borderRadius:'6px', flex:'1', minWidth:'120px' },
  btn:   { padding:'10px 20px', backgroundColor:'#1e3a5f', color:'white', border:'none', borderRadius:'6px', cursor:'pointer', fontWeight:'bold' },
  error: { background:'#ffebee', color:'#c62828', padding:'10px', borderRadius:'6px', marginBottom:'12px', fontSize:'0.9rem' },
  empty: { color:'#999' },
  table: { width:'100%', borderCollapse:'collapse' },
  secondaryBtn: { padding:'8px 14px', backgroundColor:'#c62828', color:'white', border:'none', borderRadius:'6px', cursor:'pointer', fontWeight:'bold' },
  filaAnulada: { color:'#757575', textDecoration:'line-through' },
  badgeAnulada: { background:'#fce4ec', color:'#ad1457', padding:'6px 10px', borderRadius:'999px', fontSize:'0.8rem', fontWeight:'bold' },
};
