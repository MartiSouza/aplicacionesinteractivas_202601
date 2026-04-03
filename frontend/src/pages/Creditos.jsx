import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchCreditosPorCliente, addCredito, clearCreditos } from '../store/slices/creditosSlice';

// Función para formatear fechas
const formatearFecha = (fechaString) => {
  if (!fechaString) return '';
  const fecha = new Date(fechaString + 'T00:00:00'); // Agregar hora para evitar problemas de zona horaria
  return fecha.toLocaleDateString('es-ES', { year: 'numeric', month: 'long', day: 'numeric' });
};

export default function Creditos() {
  const dispatch = useDispatch();
  const { lista, loading, error } = useSelector((state) => state.creditos);
  const [dni, setDni]   = useState('');
  const [buscado, setBuscado] = useState(false);
  const [form, setForm] = useState({ dniCliente:'', deudaOriginal:'', fecha:'', importeCuota:'', cantidadCuotas:'' });

  const buscar = async (e) => {
    e.preventDefault();
    dispatch(clearCreditos());
    const result = await dispatch(fetchCreditosPorCliente(dni));
    if (result.meta.requestStatus === 'fulfilled') setBuscado(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = {
      ...form,
      deudaOriginal:  Number(form.deudaOriginal),
      importeCuota:   Number(form.importeCuota),
      cantidadCuotas: Number(form.cantidadCuotas),
    };
    const result = await dispatch(addCredito(payload));
    if (result.meta.requestStatus === 'fulfilled') {
      setForm({ dniCliente:'', deudaOriginal:'', fecha:'', importeCuota:'', cantidadCuotas:'' });
      if (form.dniCliente === dni) dispatch(fetchCreditosPorCliente(dni));
    }
  };

  return (
    <div style={styles.page}>
      <h2 style={styles.title}>Créditos</h2>

      <div style={styles.card}>
        <h3>Buscar créditos por cliente</h3>
        <form onSubmit={buscar} style={styles.row}>
          <input style={styles.input} placeholder="DNI del cliente" value={dni} onChange={e => setDni(e.target.value)} required />
          <button style={styles.btn}>Buscar</button>
        </form>
      </div>

      <div style={styles.card}>
        <h3>Nuevo crédito</h3>
        {error && <div style={styles.error}>{error}</div>}
        <form onSubmit={handleSubmit} style={styles.grid}>
          <input style={styles.input} placeholder="DNI cliente"          value={form.dniCliente}     onChange={e => setForm({...form, dniCliente: e.target.value})}     required />
          <input style={styles.input} placeholder="Deuda original"       value={form.deudaOriginal}  onChange={e => setForm({...form, deudaOriginal: e.target.value})}  type="number" required />
          <input style={styles.input} placeholder="Fecha"                value={form.fecha}          onChange={e => setForm({...form, fecha: e.target.value})}          type="date"   required />
          <input style={styles.input} placeholder="Importe cuota"        value={form.importeCuota}   onChange={e => setForm({...form, importeCuota: e.target.value})}   type="number" required />
          <input style={styles.input} placeholder="Cant. cuotas"         value={form.cantidadCuotas} onChange={e => setForm({...form, cantidadCuotas: e.target.value})} type="number" min="1" required />
          <button style={{...styles.btn, gridColumn:'span 2'}} disabled={loading}>{loading ? 'Guardando...' : 'Crear crédito'}</button>
        </form>
      </div>

      {buscado && (
        <div style={styles.card}>
          <h3>Créditos del cliente ({lista.length})</h3>
          {loading && <p style={styles.empty}>Cargando...</p>}
          {!loading && lista.length === 0 && <p style={styles.empty}>Sin créditos.</p>}
          {lista.map(cr => (
            <div key={cr.id} style={styles.creditoBox}>
              <p><strong>ID #{cr.id}</strong> — Deuda: ${cr.deudaOriginal} — {cr.cantidadCuotas} cuotas de ${cr.importeCuota}</p>
              <table style={styles.table}>
                <thead><tr><th>#</th><th>Vencimiento</th><th>Estado</th></tr></thead>
                <tbody>
                  {cr.cuotas.map(c => (
                    <tr key={c.idCuota}>
                      <td>{c.idCuota}</td>
                      <td>{formatearFecha(c.fechaVencimiento)}</td>
                      <td style={{ color: c.pagada ? '#2e7d32' : '#c62828' }}>{c.pagada ? '✔ Pagada' : '✘ Pendiente'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

const styles = {
  page:       { padding:'32px', maxWidth:'900px', margin:'0 auto' },
  title:      { color:'#1e3a5f', marginBottom:'24px' },
  card:       { background:'white', padding:'24px', borderRadius:'12px', boxShadow:'0 2px 10px rgba(0,0,0,0.08)', marginBottom:'24px' },
  row:        { display:'flex', gap:'12px' },
  grid:       { display:'grid', gridTemplateColumns:'1fr 1fr', gap:'12px' },
  input:      { padding:'10px', border:'1px solid #ccc', borderRadius:'6px', width:'100%', boxSizing:'border-box' },
  btn:        { padding:'10px 20px', backgroundColor:'#1e3a5f', color:'white', border:'none', borderRadius:'6px', cursor:'pointer', fontWeight:'bold' },
  error:      { background:'#ffebee', color:'#c62828', padding:'10px', borderRadius:'6px', marginBottom:'12px', fontSize:'0.9rem' },
  empty:      { color:'#999' },
  creditoBox: { borderLeft:'4px solid #1e3a5f', paddingLeft:'16px', marginBottom:'20px' },
  table:      { width:'100%', borderCollapse:'collapse', marginTop:'8px' },
};
