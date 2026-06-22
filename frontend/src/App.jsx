import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import PrivateRoute from './components/PrivateRoute';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import Clientes from './pages/Clientes';
import Creditos from './pages/Creditos';
import Cobranzas from './pages/Cobranzas';
import Dashboard from './pages/Dashboard';
import GestorPermisos from './pages/GestorPermisos';

export default function App() {
  const user = useSelector((state) => state.auth.user);

  return (
    <BrowserRouter>
      <Navbar />
      <Routes>
        <Route path="/"         element={<Navigate to={user ? '/dashboard' : '/login'} replace />} />
        <Route path="/login"     element={<Login />} />
        <Route path="/register"  element={<Register />} />
        <Route path="/dashboard" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
        <Route path="/clientes"  element={<PrivateRoute><Clientes /></PrivateRoute>} />
        <Route path="/creditos"  element={<PrivateRoute><Creditos /></PrivateRoute>} />
        <Route path="/cobranzas" element={<PrivateRoute><Cobranzas /></PrivateRoute>} />
        <Route path="/admin/permisos" element={<PrivateRoute requiredRole="ADMIN"><GestorPermisos /></PrivateRoute>} />
        <Route path="*"          element={<Navigate to={user ? '/dashboard' : '/login'} replace />} />
      </Routes>
    </BrowserRouter>
  );
}
