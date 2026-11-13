import { useRequest } from 'ahooks';
import { useNavigate, useParams } from 'react-router-dom';
import { ErrorBox } from '../../../components/ui/ErrorBox';
import { Loading } from '../../../components/ui/Loading';
import { templateApi } from '../api/templateApi';
import { TemplateModal } from '../components/TemplateModal';
import { useCreateResume } from '../hooks/useCreateResume';

export function TemplateDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { createResume, loading: creating } = useCreateResume();
  const { data, error, loading } = useRequest(() => templateApi.get(id), {
    refreshDeps: [id],
    retryCount: 1,
  });
  if (error) return <ErrorBox error={error} />;
  if (loading || !data || String(data.id) !== id) return <Loading />;
  return (
    <main className="page-shell">
      <TemplateModal
        template={data}
        busy={creating}
        onUse={() => createResume(data)}
        onClose={() => navigate('/templates')}
      />
    </main>
  );
}
