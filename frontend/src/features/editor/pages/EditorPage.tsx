import { useQuery } from '@tanstack/react-query';
import { useParams } from 'react-router-dom';
import { resumeApi } from '../../../api/resumeApi';
import { ErrorBox } from '../../../components/ui/ErrorBox';
import { Loading } from '../../../components/ui/Loading';
import { ResumeEditor } from '../components/ResumeEditor';

export default function EditorPage() {
  const { id } = useParams();
  const query = useQuery({
    queryKey: ['resume', id],
    queryFn: () => resumeApi.get(id!),
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
  if (query.isFetching) return <Loading />;
  if (query.error) return <ErrorBox error={query.error} retry={() => void query.refetch()} />;
  if (!query.data) return null;
  return <ResumeEditor key={id} initialResume={query.data} />;
}
