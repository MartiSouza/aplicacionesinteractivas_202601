import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchDashboardResumen } from '../store/slices/dashboardSlice';

const formatCurrency = (value) => Number(value ?? 0).toLocaleString('es-AR', {
  style: 'currency',
  currency: 'ARS',
  minimumFractionDigits: 2,
});

const formatPercentage = (value) => `${Number(value ?? 0).toLocaleString('es-AR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})}%`;

export default function Dashboard() {
  const dispatch = useDispatch();
  const { resumen, loading, error } = useSelector((state) => state.dashboard);

  useEffect(() => {
    // CAMBIO: cargar el resumen del dashboard al montar la pantalla.
    dispatch(fetchDashboardResumen());
  }, [dispatch]);

  return (
    <div style={styles.page}>
      <h2 style={styles.title}>Dashboard</h2>

      {loading && (
        <div style={styles.card}>
          <p style={styles.empty}>Cargando métricas...</p>
        </div>
      )}

      {error && (
        <div style={styles.card}>
          <div style={styles.error}>{error}</div>
        </div>
      )}

      {!loading && !error && resumen && (
        <>
          <div style={styles.grid}>
            <div style={styles.metricCard}>
              <span style={styles.metricLabel}>Total de créditos</span>
              <strong style={styles.metricValue}>{resumen.totalCreditos}</strong>
            </div>
            <div style={styles.metricCard}>
              <span style={styles.metricLabel}>Monto total prestado</span>
              <strong style={styles.metricValue}>{formatCurrency(resumen.montoTotalPrestado)}</strong>
            </div>
            <div style={styles.metricCard}>
              <span style={styles.metricLabel}>Monto total cobrado</span>
              <strong style={styles.metricValue}>{formatCurrency(resumen.montoTotalCobrado)}</strong>
            </div>
            <div style={styles.metricCard}>
              <span style={styles.metricLabel}>Porcentaje de recupero</span>
              <strong style={styles.metricValue}>{formatPercentage(resumen.porcentajeRecupero)}</strong>
            </div>
            <div style={styles.metricCard}>
              <span style={styles.metricLabel}>Créditos activos</span>
              <strong style={styles.metricValue}>{resumen.cantidadCreditosActivos}</strong>
            </div>
            <div style={styles.metricCard}>
              <span style={styles.metricLabel}>Créditos en mora</span>
              <strong style={styles.metricValue}>{resumen.cantidadCreditosEnMora}</strong>
            </div>
          </div>

          <div style={styles.card}>
            <h3 style={styles.subtitle}>Créditos por estado</h3>
            {resumen.creditosPorEstado.length === 0 ? (
              <p style={styles.empty}>No hay métricas por estado para mostrar.</p>
            ) : (
              <table style={styles.table}>
                <thead>
                  <tr>
                    <th>Estado</th>
                    <th>Cantidad</th>
                  </tr>
                </thead>
                <tbody>
                  {resumen.creditosPorEstado.map((item) => (
                    <tr key={item.estado}>
                      <td>{item.estado}</td>
                      <td>{item.cantidad}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}
    </div>
  );
}

const styles = {
  page: { padding: '32px', maxWidth: '1000px', margin: '0 auto' },
  title: { color: '#1e3a5f', marginBottom: '24px' },
  subtitle: { color: '#1e3a5f', marginBottom: '16px' },
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
    gap: '16px',
    marginBottom: '24px',
  },
  card: {
    background: 'white',
    padding: '24px',
    borderRadius: '12px',
    boxShadow: '0 2px 10px rgba(0,0,0,0.08)',
    marginBottom: '24px',
  },
  metricCard: {
    background: 'white',
    padding: '20px',
    borderRadius: '12px',
    boxShadow: '0 2px 10px rgba(0,0,0,0.08)',
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  },
  metricLabel: { color: '#546e7a', fontSize: '0.95rem' },
  metricValue: { color: '#1e3a5f', fontSize: '1.5rem' },
  error: {
    background: '#ffebee',
    color: '#c62828',
    padding: '10px',
    borderRadius: '6px',
    fontSize: '0.9rem',
  },
  empty: { color: '#999' },
  table: { width: '100%', borderCollapse: 'collapse' },
};