import { describe, expect, it } from 'vitest';
import dashboardReducer, { fetchDashboardResumen, fetchDashboardCreditosPorEstado } from './dashboardSlice';

describe('dashboardSlice', () => {
  it('devuelve el estado inicial', () => {
    // CAMBIO: validar estado inicial del reducer.
    const state = dashboardReducer(undefined, { type: '@@INIT' });

    expect(state).toEqual({
      resumen: null,
      creditosPorEstado: [],
      loading: false,
      error: null,
      pendingRequests: 0,
    });
  });

  it('maneja pending', () => {
    // CAMBIO: validar transición a loading durante la carga.
    const state = dashboardReducer(undefined, fetchDashboardResumen.pending('', undefined));

    expect(state.loading).toBe(true);
    expect(state.error).toBeNull();
    expect(state.resumen).toBeNull();
    expect(state.creditosPorEstado).toEqual([]);
  });

  it('maneja fulfilled', () => {
    // CAMBIO: validar carga correcta del resumen.
    const payload = {
      totalCreditos: 4,
      montoTotalPrestado: 100000,
      montoTotalCobrado: 25000,
      porcentajeRecupero: 25,
      cantidadCreditosActivos: 3,
      cantidadCreditosEnMora: 1,
      creditosPorEstado: [{ estado: 'ACTIVO', cantidad: 3 }],
    };

    const state = dashboardReducer(undefined, fetchDashboardResumen.fulfilled(payload, '', undefined));

    expect(state.loading).toBe(false);
    expect(state.error).toBeNull();
    expect(state.resumen).toEqual(payload);
    expect(state.creditosPorEstado).toEqual(payload.creditosPorEstado);
  });

  it('maneja rejected', () => {
    // CAMBIO: validar error cuando falla la carga.
    const action = {
      type: fetchDashboardResumen.rejected.type,
      payload: 'No se pudo obtener el dashboard',
    };

    const state = dashboardReducer(undefined, action);

    expect(state.loading).toBe(false);
    expect(state.error).toBe('No se pudo obtener el dashboard');
  });

  it('maneja creditos por estado fulfilled', () => {
    const payload = [{ estado: 'ACTIVO', cantidad: 2 }];

    const state = dashboardReducer(undefined, fetchDashboardCreditosPorEstado.fulfilled(payload, '', undefined));

    expect(state.creditosPorEstado).toEqual(payload);
    expect(state.loading).toBe(false);
    expect(state.error).toBeNull();
  });
});