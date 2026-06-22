import { configureStore } from '@reduxjs/toolkit';
import authReducer      from './slices/authSlice';
import clientesReducer  from './slices/clientesSlice';
import creditosReducer  from './slices/creditosSlice';
import cobranzasReducer from './slices/cobranzasSlice';
import dashboardReducer from './slices/dashboardSlice';
import permisosReducer from './slices/permisosSlice';

const store = configureStore({
  reducer: {
    auth:      authReducer,
    clientes:  clientesReducer,
    creditos:  creditosReducer,
    cobranzas: cobranzasReducer,
    dashboard: dashboardReducer,
    permisos: permisosReducer,
  },
});

export default store;
