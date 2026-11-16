import { useLockFn, useRequest } from 'ahooks';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '../../../components/ui/Button';
import { ErrorBox } from '../../../components/ui/ErrorBox';
import { useSession } from '../../../stores/sessionStore';
import type { Content, Template } from '../../../types';
import { ResumePreview } from '../../resumes/components/ResumePreview';
import { aiApi } from '../api/aiApi';

export function GeneratedResumePreview({
  content,
  initialTitle,
  templates,
  onCreatingChange,
  disabled,
}: {
  content: Content;
  initialTitle: string;
  templates: Template[];
  onCreatingChange: (creating: boolean) => void;
  disabled: boolean;
}) {
  const user = useSession((session) => session.user)!;
  const [title, setTitle] = useState(initialTitle);
  const [templateId, setTemplateId] = useState<number | undefined>();
  const availableTemplates = templates.filter((template) => !template.isVip || user.member);
  const template =
    availableTemplates.find((item) => item.id === templateId) ?? availableTemplates[0];
  const navigate = useNavigate();
  const creation = useRequest(
    async () => {
      if (!template) throw new Error('请先选择可用的简历模板');
      const token = useSession.getState().token;
      const resume = await aiApi.createResume(
        { templateId: template.id, title: title.trim(), content },
        token ?? undefined,
      );
      return { resume, token };
    },
    {
      manual: true,
      onBefore: () => onCreatingChange(true),
      onFinally: () => onCreatingChange(false),
      onSuccess(response) {
        if (useSession.getState().token === response.token)
          navigate(`/editor/${response.resume.id}`);
      },
    },
  );
  const create = useLockFn(() => creation.runAsync().catch(() => {}));
  return (
    <section className="ai-generated-preview" aria-label="生成的简历预览">
      <h2>确认简历内容</h2>
      <label>
        简历标题
        <input
          maxLength={100}
          required
          value={title}
          disabled={creation.loading}
          onChange={(event) => setTitle(event.target.value)}
        />
      </label>
      <label>
        选择模板
        <select
          value={template?.id ?? ''}
          disabled={creation.loading || !template}
          onChange={(event) => setTemplateId(Number(event.target.value))}
        >
          {!template && <option value="">暂无可用模板</option>}
          {availableTemplates.map((item) => (
            <option key={item.id} value={item.id}>
              {item.name}
              {item.isVip ? ' · 会员' : ''}
            </option>
          ))}
        </select>
      </label>
      {template && (
        <div className="ai-preview-canvas">
          <div className="ai-preview-sheet">
            <ResumePreview content={content} template={template} watermark={!user.member} />
          </div>
        </div>
      )}
      {creation.error && <ErrorBox error={creation.error} />}
      <Button
        className="primary full"
        busy={creation.loading}
        disabled={disabled || !title.trim() || !template}
        onClick={() => void create()}
      >
        确认内容并创建简历
      </Button>
    </section>
  );
}
