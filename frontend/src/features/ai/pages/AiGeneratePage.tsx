import { useBoolean } from 'ahooks';
import { Link } from 'react-router-dom';
import { ErrorBox } from '../../../components/ui/ErrorBox';
import { Loading } from '../../../components/ui/Loading';
import { useSession } from '../../../stores/sessionStore';
import { useTemplates } from '../../templates/hooks/useTemplates';
import { AiServiceNotice } from '../components/AiServiceNotice';
import { GeneratedResumePreview } from '../components/GeneratedResumePreview';
import { ResumeConversation } from '../components/ResumeConversation';
import { useAiStatus } from '../hooks/useAiStatus';
import { useResumeConversation } from '../hooks/useResumeConversation';

function AiGenerateContent() {
  const status = useAiStatus();
  const templates = useTemplates();
  const conversation = useResumeConversation();
  const [creating, { set: setCreating }] = useBoolean(false);
  const generated = conversation.generated;
  return (
    <main className="page-shell ai-generate-page">
      <Link className="back-link" to="/me">
        ← 返回工作台
      </Link>
      <header className="ai-page-heading">
        <h1>AI 生成简历</h1>
        <p>通过对话整理经历，确认生成结果后创建简历。</p>
      </header>
      <AiServiceNotice {...status} />
      {templates.error && <ErrorBox error={templates.error} retry={templates.refresh} />}
      <div className="ai-generation-layout">
        <ResumeConversation
          conversation={conversation}
          available={Boolean(status.data?.available)}
          creating={creating}
        />
        {generated?.content ? (
          <GeneratedResumePreview
            key={conversation.messages.length}
            content={generated.content}
            initialTitle={generated.title || '我的简历'}
            templates={templates.data ?? []}
            onCreatingChange={setCreating}
            disabled={conversation.loading}
          />
        ) : (
          <section className="ai-preview-placeholder">
            <h2>简历预览</h2>
            <p>补充经历后点击“生成简历”，在这里查看完整内容。</p>
            {templates.loading && <Loading />}
          </section>
        )}
      </div>
    </main>
  );
}

export function AiGeneratePage() {
  const token = useSession((session) => session.token);
  return <AiGenerateContent key={token} />;
}
