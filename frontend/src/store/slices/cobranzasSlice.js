import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { getCobranzasPorCredito, registrarCobranza, anularCobranza as anularCobranzaApi } from '../../api/cobranzas';

export const fetchCobranzasPorCredito = createAsyncThunk('cobranzas/fetchPorCredito', async (idCredito, { rejectWithValue }) => {
  try {
    return await getCobranzasPorCredito(idCredito);
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

export const addCobranza = createAsyncThunk('cobranzas/add', async (data, { rejectWithValue }) => {
  try {
    return await registrarCobranza(data);
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

export const anularCobranza = createAsyncThunk('cobranzas/anular', async (id, { rejectWithValue }) => {
  try {
    return await anularCobranzaApi(id);
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

const cobranzasSlice = createSlice({
  name: 'cobranzas',
  initialState: {
    lista:   [],
    loading: false,
    error:   null,
  },
  reducers: {
    clearCobranzas(state) { state.lista = []; },
    clearError(state)     { state.error = null; },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchCobranzasPorCredito.pending,   (state) => { state.loading = true;  state.error = null; })
      .addCase(fetchCobranzasPorCredito.fulfilled, (state, action) => { state.loading = false; state.lista = action.payload; })
      .addCase(fetchCobranzasPorCredito.rejected,  (state, action) => { state.loading = false; state.error = action.payload; })
      .addCase(addCobranza.pending,                (state) => { state.loading = true;  state.error = null; })
      .addCase(addCobranza.fulfilled,              (state, action) => { state.loading = false; state.lista.push(action.payload); })
      .addCase(addCobranza.rejected,               (state, action) => { state.loading = false; state.error = action.payload; })
      .addCase(anularCobranza.pending,             (state) => { state.loading = true; state.error = null; })
      .addCase(anularCobranza.fulfilled,           (state, action) => {
        state.loading = false;
        state.lista = state.lista.map((cobranza) => cobranza.id === action.payload.id ? action.payload : cobranza);
      })
      .addCase(anularCobranza.rejected,            (state, action) => { state.loading = false; state.error = action.payload; });
  },
});

export const { clearCobranzas, clearError } = cobranzasSlice.actions;
export default cobranzasSlice.reducer;
