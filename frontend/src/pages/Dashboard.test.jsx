import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { describe, expect, it, vi } from 'vitest';
import Dashboard from './Dashboard';
import dashboardReducer from '../store/slices/dashboardSlice';

vi.mock('../store/slices/dashboardSlice', async () => {
  const actual = await vi.importActual('../store/slices/dashboardSlice');

  return {
    ...actual,
    // CAMBIO: evitar llamada real y dejar el efecto estable para los tests de render.
    fetchDashboardResumen: vi.fn(() => ({ type: 'dashboard/fetchResumen' })),
  };
});

const renderWithState = (dashboardState) => {
  const store = configureStore({
    reducer: {
      dashboard: dashboardReducer,
    },
    preloadedState: {
      dashboard: dashboardState,
    },
  });

  return render(
    <Provider store={store}>
      <Dashboard />
    </Provider>,
  );
};

describe('Dashboard', () => {
  it('muestra loading', () => {
    // CAMBIO: validar estado visual de carga.
    renderWithState({
      resumen: null,
      loading: true,
      error: null,
    });

    expect(screen.getByText('Cargando métricas...')).toBeInTheDocument();
  });

  it('muestra error', () => {
    // CAMBIO: validar render cuando hay error.
    renderWithState({
      resumen: null,
      loading: false,
      error: 'Error al cargar dashboard',
    });

    expect(screen.getByText('Error al cargar dashboard')).toBeInTheDocument();
  });

  it('muestra métricas con datos', () => {
    // CAMBIO: validar render principal del resumen cargado.
    renderWithState({
      loading: false,
      error: null,
      resumen: {
        totalCreditos: 10,
        montoTotalPrestado: 150000,
        montoTotalCobrado: 50000,
        porcentajeRecupero: 33.33,
        cantidadCreditosActivos: 7,
        cantidadCreditosEnMora: 3,
        creditosPorEstado: [
          { estado: 'ACTIVO', cantidad: 7 },
          { estado: 'EN_MORA', cantidad: 3 },
        ],
      },
    });

    expect(screen.getByRole('heading', { name: 'Dashboard' })).toBeInTheDocument();
    expect(screen.getByText('Total de créditos')).toBeInTheDocument();
    expect(screen.getByText('10')).toBeInTheDocument();
    expect(screen.getByText('Créditos por estado')).toBeInTheDocument();
    expect(screen.getByText('ACTIVO')).toBeInTheDocument();
    expect(screen.getByText('EN_MORA')).toBeInTheDocument();
  });
});