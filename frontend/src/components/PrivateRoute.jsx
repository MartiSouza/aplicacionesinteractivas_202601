import { Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';

// CAMBIO: la ruta protegida admite además un rol requerido para vistas exclusivas de ADMIN.
export default function PrivateRoute({ children, requiredRole }) {
  const user = useSelector((state) => state.auth.user);

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && user.rol !== requiredRole) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}
