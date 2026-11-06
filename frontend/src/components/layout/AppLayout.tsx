import { Outlet, useLocation } from 'react-router-dom';
import { Toast } from '../ui/Toast';
import { SiteFooter } from './SiteFooter';
import { SiteHeader } from './SiteHeader';
export function AppLayout() {
  const location = useLocation();
  return (
    <>
      <SiteHeader />
      <Outlet />
      {!location.pathname.startsWith('/editor') && <SiteFooter />}
      <Toast />
    </>
  );
}
