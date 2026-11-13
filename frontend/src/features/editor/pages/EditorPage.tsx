import { useRequest } from 'ahooks';
import { useParams } from 'react-router-dom';
import { resumeApi } from '../../resumes/api/resumeApi';
import { ErrorBox } from '../../../components/ui/ErrorBox';
import { Loading } from '../../../components/ui/Loading';
import { ResumeEditor } from '../components/ResumeEditor';

function EditorContent({ resumeId }: { resumeId: string }) {
  const { data, error, loading, refresh } = useRequest(() => resumeApi.get(resumeId), {
    retryCount: 1,
  });
  if (loading) return <Loading />;
  if (error) return <ErrorBox error={error} retry={refresh} />;
  if (!data) return null;
  return <ResumeEditor initialResume={data} />;
}

export default function EditorPage() {
  const { id } = useParams();
  return id ? <EditorContent key={id} resumeId={id} /> : null;
}
