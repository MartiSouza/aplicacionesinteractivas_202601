import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { getCreditosPorCliente, crearCredito, anularCredito as anularCreditoApi } from '../../api/creditos';

export const fetchCreditosPorCliente = createAsyncThunk('creditos/fetchPorCliente', async (dni, { rejectWithValue }) => {
  try {
    return await getCreditosPorCliente(dni);
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

export const addCredito = createAsyncThunk('creditos/add', async (data, { rejectWithValue }) => {
  try {
    return await crearCredito(data);
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

export const anularCredito = createAsyncThunk('creditos/anular', async (id, { rejectWithValue }) => {
  try {
    return await anularCreditoApi(id);
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

const creditosSlice = createSlice({
  name: 'creditos',
  initialState: {
    lista:   [],
    loading: false,
    error:   null,
  },
  reducers: {
    clearCreditos(state) { state.lista = []; },
    clearError(state)    { state.error = null; },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchCreditosPorCliente.pending,   (state) => { state.loading = true;  state.error = null; })
      .addCase(fetchCreditosPorCliente.fulfilled, (state, action) => { state.loading = false; state.lista = action.payload; })
      .addCase(fetchCreditosPorCliente.rejected,  (state, action) => { state.loading = false; state.error = action.payload; })
      .addCase(addCredito.pending,                (state) => { state.loading = true;  state.error = null; })
      .addCase(addCredito.fulfilled,              (state, action) => { state.loading = false; state.lista.push(action.payload); })
      .addCase(addCredito.rejected,               (state, action) => { state.loading = false; state.error = action.payload; })
      .addCase(anularCredito.pending,             (state) => { state.loading = true; state.error = null; })
      .addCase(anularCredito.fulfilled,           (state, action) => {
        state.loading = false;
        state.lista = state.lista.map((credito) => credito.id === action.payload.id ? action.payload : credito);
      })
      .addCase(anularCredito.rejected,            (state, action) => { state.loading = false; state.error = action.payload; });
  },
});

export const { clearCreditos, clearError } = creditosSlice.actions;
export default creditosSlice.reducer;
