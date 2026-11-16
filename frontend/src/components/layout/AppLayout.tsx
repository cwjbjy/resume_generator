import { Outlet, useLocation } from 'react-router-dom';
import { useSession } from '../../stores/sessionStore';
import { Toast } from '../ui/Toast';
import { SiteFooter } from './SiteFooter';
import { SiteHeader } from './SiteHeader';
export function AppLayout() {
  const location = useLocation();
  const userId = useSession((session) => session.user?.id);
  return (
    <>
      <SiteHeader />
      <Outlet key={userId} />
      {!location.pathname.startsWith('/editor') && <SiteFooter />}
      <Toast />
    </>
  );
}
