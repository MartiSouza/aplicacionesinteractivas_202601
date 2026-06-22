import { Link, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { logout } from '../store/slices/authSlice';

export default function Navbar() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const user     = useSelector((state) => state.auth.user);

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  return (
    <nav style={styles.nav}>
      <span style={styles.brand}>💳 Créditos UADE</span>
      {user && (
        <div style={styles.links}>
          <Link to="/dashboard" style={styles.link}>Dashboard</Link>
          <Link to="/clientes"  style={styles.link}>Clientes</Link>
          <Link to="/creditos"  style={styles.link}>Créditos</Link>
          <Link to="/cobranzas" style={styles.link}>Cobranzas</Link>
          {/* CAMBIO: acceso al gestor de permisos solo visible para ADMIN. */}
          {user.rol === 'ADMIN' && <Link to="/admin/permisos" style={styles.link}>Permisos</Link>}
          <span style={styles.user}>👤 {user.username}</span>
          <button onClick={handleLogout} style={styles.btn}>Salir</button>
        </div>
      )}
    </nav>
  );
}

const styles = {
  nav:   { display:'flex', justifyContent:'space-between', alignItems:'center', padding:'12px 24px', backgroundColor:'#1e3a5f', color:'white' },
  brand: { fontWeight:'bold', fontSize:'1.2rem' },
  links: { display:'flex', alignItems:'center', gap:'20px' },
  link:  { color:'#90caf9', textDecoration:'none', fontWeight:'500' },
  user:  { color:'#b0bec5', fontSize:'0.9rem' },
  btn:   { background:'#e53935', color:'white', border:'none', padding:'6px 14px', borderRadius:'6px', cursor:'pointer' },
};
