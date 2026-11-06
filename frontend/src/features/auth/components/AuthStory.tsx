import { CheckCircle2 } from 'lucide-react';
import { ResumePreview } from '../../../components/resume/ResumePreview';
import { sample } from '../../../data/sampleResume';
import type { Template } from '../../../types';

// 登录页插画使用固定的示例样式，不参与真实模板目录
const storyTemplate: Template = {
  id: 0,
  name: '示例简历',
  category: '通用',
  isVip: false,
  color: '#3568d4',
  style: 'modern',
  usedCount: 0,
};

export function AuthStory() {
  return (
    <div className="auth-story">
      <div className="eyebrow">YOUR STORY. WELL TOLD.</div>
      <h1>
        下一份心动的工作，
        <br />
        从看见你的价值开始。
      </h1>
      <p>整理每一段成长，认真书写新的可能。</p>
      <div className="auth-illustration">
        <div className="auth-resume">
          <ResumePreview content={sample} template={storyTemplate} />
        </div>
        <div className="auth-success">
          <CheckCircle2 size={23} />
          <div>
            已准备好，向未来出发<small>每一份努力，都值得被看见</small>
          </div>
        </div>
      </div>
    </div>
  );
}
