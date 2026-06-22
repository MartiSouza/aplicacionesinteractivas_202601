import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { getDashboardResumen, getDashboardCreditosPorEstado } from '../../api/dashboard';

export const fetchDashboardResumen = createAsyncThunk('dashboard/fetchResumen', async (_, { rejectWithValue }) => {
  try {
    return await getDashboardResumen();
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

export const fetchDashboardCreditosPorEstado = createAsyncThunk('dashboard/fetchCreditosPorEstado', async (_, { rejectWithValue }) => {
  try {
    return await getDashboardCreditosPorEstado();
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

const beginRequest = (state) => {
  state.pendingRequests += 1;
  state.loading = true;
  state.error = null;
};

const endRequest = (state) => {
  state.pendingRequests = Math.max(0, state.pendingRequests - 1);
  state.loading = state.pendingRequests > 0;
};

const dashboardSlice = createSlice({
  name: 'dashboard',
  initialState: {
    resumen: null,
    creditosPorEstado: [],
    loading: false,
    error: null,
    pendingRequests: 0,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchDashboardResumen.pending, (state) => {
        beginRequest(state);
      })
      .addCase(fetchDashboardResumen.fulfilled, (state, action) => {
        endRequest(state);
        state.resumen = action.payload;
        state.creditosPorEstado = action.payload.creditosPorEstado ?? [];
      })
      .addCase(fetchDashboardResumen.rejected, (state, action) => {
        endRequest(state);
        state.error = action.payload;
      })
      .addCase(fetchDashboardCreditosPorEstado.pending, (state) => {
        beginRequest(state);
      })
      .addCase(fetchDashboardCreditosPorEstado.fulfilled, (state, action) => {
        endRequest(state);
        state.creditosPorEstado = action.payload;
      })
      .addCase(fetchDashboardCreditosPorEstado.rejected, (state, action) => {
        endRequest(state);
        state.error = action.payload;
      });
  },
});

export default dashboardSlice.reducer;