import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useSession } from '../../../stores/sessionStore';
export function ProtectedRoute() {
  const token = useSession((s) => s.token);
  const l = useLocation();
  return token ? (
    <Outlet />
  ) : (
    <Navigate replace to={`/login?redirect=${encodeURIComponent(l.pathname + l.search)}`} />
  );
}
