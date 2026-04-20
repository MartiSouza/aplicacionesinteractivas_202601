import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { getDashboardResumen } from '../../api/dashboard';

// CAMBIO: thunk para obtener el resumen del dashboard desde el backend.
export const fetchDashboardResumen = createAsyncThunk('dashboard/fetchResumen', async (_, { rejectWithValue }) => {
  try {
    return await getDashboardResumen();
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

const dashboardSlice = createSlice({
  name: 'dashboard',
  initialState: {
    resumen: null,
    loading: false,
    error: null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchDashboardResumen.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchDashboardResumen.fulfilled, (state, action) => {
        state.loading = false;
        state.resumen = action.payload;
      })
      .addCase(fetchDashboardResumen.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

export default dashboardSlice.reducer;