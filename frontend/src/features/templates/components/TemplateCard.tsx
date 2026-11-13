import { ArrowUpRight, Crown, FileText } from 'lucide-react';
import { ResumePreview } from '../../resumes/components/ResumePreview';
import { sample } from '../../../data/sampleResume';
import type { Template } from '../../../types';

const descriptions: Record<number, string> = {
  1: '清晰利落，适合多种岗位',
  2: '稳重专业，展现职场实力',
  3: '轻盈明快，开启职业第一步',
  4: '留白有度，让内容成为主角',
};
interface TemplateCardProps {
  template: Template;
  busy: boolean;
  onPreview: () => void;
  onUse: () => void;
}
export function TemplateCard({ template, busy, onPreview, onUse }: TemplateCardProps) {
  return (
    <div className="template-card">
      <button
        className={`template-cover cover-${template.style}`}
        onClick={onPreview}
        aria-label={`预览${template.name}`}
      >
        <span className={`template-tag ${template.isVip ? 'vip' : ''}`}>
          {template.isVip ? (
            <>
              <Crown size={11} />
              会员专享
            </>
          ) : (
            '免费使用'
          )}
        </span>
        <div className="mini-resume">
          <ResumePreview content={sample} template={template} />
        </div>
        <div className="cover-overlay">
          <span>
            预览模板 <ArrowUpRight size={16} />
          </span>
        </div>
      </button>
      <div className="template-card-info">
        <div>
          <h3>{template.name}</h3>
          <span>{descriptions[template.id] || '原创设计，清晰表达'}</span>
        </div>
        <button
          className="template-use"
          aria-label={`使用${template.name}`}
          disabled={busy}
          onClick={onUse}
        >
          <ArrowUpRight size={19} />
        </button>
      </div>
      <div className="card-meta">
        <span>{template.category} · A4 标准</span>
        <span>
          <FileText size={11} />
          {template.usedCount.toLocaleString()} 人使用
        </span>
      </div>
    </div>
  );
}
