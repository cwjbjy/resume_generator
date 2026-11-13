import { ArrowRight, Check } from 'lucide-react';
import { ResumePreview } from '../../resumes/components/ResumePreview';
import { Button } from '../../../components/ui/Button';
import { Modal } from '../../../components/ui/Modal';
import { sample } from '../../../data/sampleResume';
import { type Template } from '../../../types/index';
export function TemplateModal({
  template,
  onClose,
  onUse,
  busy,
}: {
  template: Template;
  onClose: () => void;
  onUse: () => void;
  busy: boolean;
}) {
  return (
    <Modal title={template.name + ' · 模板预览'} wide onClose={onClose}>
      <div className="template-detail">
        <div className="detail-preview">
          <ResumePreview content={sample} template={template} />
        </div>
        <div className="detail-info">
          <span className="eyebrow">ORIGINAL DESIGN</span>
          <h2>{template.name}</h2>
          <p>清晰的内容层级，经过打磨的字号与留白，让每一段经历都被认真阅读。</p>
          <ul className="check-list">
            <li>
              <Check size={17} />
              A4 标准，支持自动分页
            </li>
            <li>
              <Check size={17} />
              教育、工作、项目自由增减
            </li>
            <li>
              <Check size={17} />
              随时更换模板，保留全部内容
            </li>
            <li>
              <Check size={17} />
              {template.isVip ? '会员专享模板' : '免费使用，每日一次导出'}
            </li>
          </ul>
          <Button className="primary" busy={busy} onClick={onUse}>
            使用此模板 <ArrowRight size={16} />
          </Button>
        </div>
      </div>
    </Modal>
  );
}
