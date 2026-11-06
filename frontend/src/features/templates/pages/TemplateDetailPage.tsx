import { useQuery } from '@tanstack/react-query';
import { useNavigate, useParams } from 'react-router-dom';
import { ErrorBox } from '../../../components/ui/ErrorBox';
import { Loading } from '../../../components/ui/Loading';
import { api } from '../../../lib/http';
import type { Template } from '../../../types';
import { TemplateModal } from '../components/TemplateModal';
import { useCreateResume } from '../hooks/useCreateResume';

export function TemplateDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { createResume, isPending } = useCreateResume();
  const { data, error, isLoading } = useQuery({
    queryKey: ['template', id],
    queryFn: () => api<Template>('GET', `/templates/${id}`),
  });
  if (isLoading) return <Loading />;
  if (!data) return <ErrorBox error={error} />;
  return (
    <main className="page-shell">
      <TemplateModal
        template={data}
        busy={isPending}
        onUse={() => createResume(data)}
        onClose={() => navigate('/templates')}
      />
    </main>
  );
}
