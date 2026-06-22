import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchDashboardResumen, fetchDashboardCreditosPorEstado } from '../store/slices/dashboardSlice';
import './Dashboard.css';

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
  const { resumen, creditosPorEstado, loading, error } = useSelector((state) => state.dashboard);

  useEffect(() => {
    dispatch(fetchDashboardResumen());
    dispatch(fetchDashboardCreditosPorEstado());
  }, [dispatch]);

  return (
    <div className="dashboard-page">
      <h2 className="dashboard-title">Dashboard</h2>

      {loading && (
        <div className="dashboard-card">
          <p className="dashboard-empty">Cargando métricas...</p>
        </div>
      )}

      {error && (
        <div className="dashboard-card">
          <div className="dashboard-error">{error}</div>
        </div>
      )}

      {!loading && !error && resumen && (
        <>
          <div className="dashboard-grid">
            <div className="dashboard-metric-card">
              <span className="dashboard-metric-label">Total de créditos</span>
              <strong className="dashboard-metric-value">{resumen.totalCreditos}</strong>
            </div>
            <div className="dashboard-metric-card">
              <span className="dashboard-metric-label">Monto total prestado</span>
              <strong className="dashboard-metric-value">{formatCurrency(resumen.montoTotalPrestado)}</strong>
            </div>
            <div className="dashboard-metric-card">
              <span className="dashboard-metric-label">Monto total cobrado</span>
              <strong className="dashboard-metric-value">{formatCurrency(resumen.montoTotalCobrado)}</strong>
            </div>
            <div className="dashboard-metric-card">
              <span className="dashboard-metric-label">Porcentaje de recupero</span>
              <strong className="dashboard-metric-value">{formatPercentage(resumen.porcentajeRecupero)}</strong>
            </div>
            <div className="dashboard-metric-card">
              <span className="dashboard-metric-label">Créditos activos</span>
              <strong className="dashboard-metric-value">{resumen.cantidadCreditosActivos}</strong>
            </div>
            <div className="dashboard-metric-card">
              <span className="dashboard-metric-label">Créditos en mora</span>
              <strong className="dashboard-metric-value">{resumen.cantidadCreditosEnMora}</strong>
            </div>
          </div>

          <div className="dashboard-card">
            <h3 className="dashboard-subtitle">Créditos por estado</h3>
            {creditosPorEstado.length === 0 ? (
              <p className="dashboard-empty">No hay métricas por estado para mostrar.</p>
            ) : (
              <table className="dashboard-table">
                <thead>
                  <tr>
                    <th>Estado</th>
                    <th>Cantidad</th>
                  </tr>
                </thead>
                <tbody>
                  {creditosPorEstado.map((item) => (
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