import { CheckCircle2 } from 'lucide-react';
import { ResumePreview } from '../../../components/resume/ResumePreview';
import { sample } from '../../../data/sampleResume';
import { templates as initialTemplates } from '../../../data/templateCatalog';

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
          <ResumePreview content={sample} template={initialTemplates[0]} />
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
