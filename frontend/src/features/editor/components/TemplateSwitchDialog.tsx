import { Check } from 'lucide-react';
import { ResumePreview } from '../../../components/resume/ResumePreview';
import { Modal } from '../../../components/ui/Modal';
import type { Content, Template } from '../../../types';

interface TemplateSwitchDialogProps {
  content: Content;
  templates: Template[];
  selectedId: number;
  onSelect: (template: Template) => void;
  onClose: () => void;
}
export function TemplateSwitchDialog({
  content,
  templates,
  selectedId,
  onSelect,
  onClose,
}: TemplateSwitchDialogProps) {
  return (
    <Modal title="换一种方式，呈现你的经历" onClose={onClose} wide>
      <p className="modal-description">切换模板会保留所有内容。</p>
      <div className="switch-template-grid">
        {templates.map((template) => (
          <button
            key={template.id}
            className={selectedId === template.id ? 'selected' : ''}
            onClick={() => onSelect(template)}
          >
            <div className="switch-preview">
              <div className="mini-resume">
                <ResumePreview content={content} template={template} />
              </div>
            </div>
            <span>
              {template.name}
              {template.isVip && <small>VIP</small>}
              {selectedId === template.id && <Check size={16} />}
            </span>
          </button>
        ))}
      </div>
    </Modal>
  );
}
