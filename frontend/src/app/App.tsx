import { Route, Routes } from 'react-router-dom';
import { AppLayout } from '../components/layout/AppLayout';
import { ProtectedRoute } from '../features/auth/components/ProtectedRoute';
import { AuthPage } from '../features/auth/pages/AuthPage';
import Editor from '../features/editor/pages/EditorPage';
import { CheckoutPage } from '../features/membership/pages/CheckoutPage';
import { PricingPage } from '../features/membership/pages/PricingPage';
import { TemplateDetailPage } from '../features/templates/pages/TemplateDetailPage';
import { TemplatesPage } from '../features/templates/pages/TemplatesPage';
import { WorkspacePage } from '../features/workspace/pages/WorkspacePage';
import { NotFoundPage } from '../pages/NotFoundPage';
export function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<TemplatesPage />} />
        <Route path="templates" element={<TemplatesPage />} />
        <Route path="templates/:id" element={<TemplateDetailPage />} />
        <Route path="login" element={<AuthPage />} />
        <Route path="register" element={<AuthPage register />} />
        <Route path="pricing" element={<PricingPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="me" element={<WorkspacePage />} />
          <Route path="editor/:id" element={<Editor />} />
          <Route path="checkout/:id" element={<CheckoutPage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}
