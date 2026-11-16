import { ArrowRight, Clock3, Copy, Pencil, Trash2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { ResumePreview } from '../../resumes/components/ResumePreview';
import { Button } from '../../../components/ui/Button';
import type { ResumeSummary, Template } from '../../../types';

interface ResumeCardProps {
  resume: ResumeSummary;
  template?: Template;
  busy: boolean;
  onRename: () => void;
  onDuplicate: () => void;
  onRemove: () => void;
}
export function ResumeCard({
  resume,
  template,
  busy,
  onRename,
  onDuplicate,
  onRemove,
}: ResumeCardProps) {
  const navigate = useNavigate();
  const edit = () => navigate(`/editor/${resume.id}`);
  return (
    <article className="my-resume-card">
      <button className="my-resume-cover" onClick={edit} aria-label={`编辑${resume.title}`}>
        <div className="mini-resume">
          {template && (
            <ResumePreview
              content={{ basic: resume.basic, edu: [], work: [], proj: [], skills: [], custom: [] }}
              template={template}
            />
          )}
        </div>
      </button>
      <div className="my-resume-info">
        <h3>{resume.title}</h3>
        <p>
          <Clock3 size={12} />
          {new Date(resume.updatedAt).toLocaleString('zh-CN', {
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
          })}
        </p>
        <div className="my-resume-actions">
          <Button className="small" onClick={edit}>
            继续编辑
            <ArrowRight size={13} />
          </Button>
          <button aria-label={`重命名${resume.title}`} className="icon-button" onClick={onRename}>
            <Pencil size={14} />
          </button>
          <button
            aria-label={`复制${resume.title}`}
            className="icon-button"
            disabled={busy}
            onClick={onDuplicate}
          >
            <Copy size={14} />
          </button>
          <button
            aria-label={`删除${resume.title}`}
            className="icon-button danger"
            onClick={onRemove}
          >
            <Trash2 size={14} />
          </button>
        </div>
      </div>
    </article>
  );
}
