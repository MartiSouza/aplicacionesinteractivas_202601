import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { getUsuarios, updatePermisos } from '../../api/admin';

// CAMBIO: thunk para cargar la tabla del gestor de permisos desde backend.
export const fetchUsuariosPermisos = createAsyncThunk('permisos/fetchUsuarios', async (_, { rejectWithValue }) => {
  try {
    return await getUsuarios();
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

// CAMBIO: thunk para persistir cambios de permisos al hacer toggle de cada checkbox.
export const updatePermisosUsuario = createAsyncThunk('permisos/updateUsuario', async ({ id, permisos }, { rejectWithValue }) => {
  try {
    return await updatePermisos(id, permisos);
  } catch (err) {
    return rejectWithValue(err.message);
  }
});

const permisosSlice = createSlice({
  name: 'permisos',
  initialState: {
    // CAMBIO: estado global específico del gestor admin de permisos.
    usuarios: [],
    loading: false,
    error: null,
    updatingUserId: null,
  },
  reducers: {
    clearError(state) {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchUsuariosPermisos.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchUsuariosPermisos.fulfilled, (state, action) => {
        state.loading = false;
        state.usuarios = action.payload;
      })
      .addCase(fetchUsuariosPermisos.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(updatePermisosUsuario.pending, (state, action) => {
        state.error = null;
        state.updatingUserId = action.meta.arg.id;
      })
      .addCase(updatePermisosUsuario.fulfilled, (state, action) => {
        state.updatingUserId = null;
        state.usuarios = state.usuarios.map((usuario) => usuario.id === action.payload.id ? action.payload : usuario);
      })
      .addCase(updatePermisosUsuario.rejected, (state, action) => {
        state.updatingUserId = null;
        state.error = action.payload;
      });
  },
});

export const { clearError } = permisosSlice.actions;
export default permisosSlice.reducer;