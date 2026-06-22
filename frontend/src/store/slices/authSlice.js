import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { login as loginApi, register as registerApi } from '../../api/auth';

// CAMBIO: se hidrata el usuario autenticado desde localStorage para sostener sesión y permisos al recargar.
const loadStoredUser = () => {
  try {
    return JSON.parse(localStorage.getItem('authUser') ?? 'null');
  } catch {
    return null;
  }
};

// CAMBIO: la respuesta de auth ahora se normaliza con rol y permisos granulares.
const normalizeAuthPayload = (payload) => ({
  token: payload.token,
  user: {
    id: payload.id,
    username: payload.username,
    rol: payload.rol,
    puedeAnularCredito: payload.puedeAnularCredito,
    puedeAnularCobranza: payload.puedeAnularCobranza,
  },
});

export const loginThunk = createAsyncThunk('auth/login', async (credentials, { rejectWithValue }) => {
  try {
    return await loginApi(credentials);
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

export const registerThunk = createAsyncThunk('auth/register', async (data, { rejectWithValue }) => {
  try {
    return await registerApi(data);
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    // CAMBIO: auth guarda usuario y token en estado global para proteger vistas y acciones.
    user: loadStoredUser(),
    token: localStorage.getItem('token'),
    loading: false,
    error: null,
  },
  reducers: {
    logout(state) {
      state.user = null;
      state.token = null;
      localStorage.removeItem('authUser');
      localStorage.removeItem('token');
    },
    clearError(state) {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    const onPending = (state) => { state.loading = true; state.error = null; };
    const onFulfilled = (state, action) => {
      const { user, token } = normalizeAuthPayload(action.payload);
      state.loading = false;
      state.user = user;
      state.token = token;
      localStorage.setItem('authUser', JSON.stringify(user));
      localStorage.setItem('token', token);
    };
    const onRejected = (state, action) => { state.loading = false; state.error = action.payload; };

    builder
      .addCase(loginThunk.pending,     onPending)
      .addCase(loginThunk.fulfilled,   onFulfilled)
      .addCase(loginThunk.rejected,    onRejected)
      .addCase(registerThunk.pending,  onPending)
      .addCase(registerThunk.fulfilled,onFulfilled)
      .addCase(registerThunk.rejected, onRejected);
  },
});

export const { logout, clearError } = authSlice.actions;
export default authSlice.reducer;
