import { Cloud } from 'lucide-react';
import type { Content } from '../../../types';
import { editorModules, type EditorModule } from '../model/editorModules';
import { BasicInfoForm } from './forms/BasicInfoForm';
import { EntryListForm } from './forms/EntryListForm';
import { SkillsForm } from './forms/SkillsForm';

interface ResumeFormProps {
  module: EditorModule;
  content: Content;
  error: string;
  onChange: (content: Content) => void;
  onRetrySave: () => void;
}
export function ResumeForm({ module, content, error, onChange, onRetrySave }: ResumeFormProps) {
  const selected = editorModules.find((item) => item.key === module)!;
  return (
    <section className="editor-form">
      <div className="editor-form-title">
        <span className="eyebrow muted">BUILD YOUR STORY</span>
        <h2>{selected.label}</h2>
        <p>{selected.description}</p>
      </div>
      {error && (
        <div className="inline-error" role="alert">
          {error}
          <button onClick={onRetrySave}>重试保存</button>
        </div>
      )}
      <div className="editor-fields">
        {module === 'basic' && (
          <BasicInfoForm
            value={content.basic}
            onChange={(basic) => onChange({ ...content, basic })}
          />
        )}
        {module === 'skills' && (
          <SkillsForm
            skills={content.skills}
            onChange={(skills) => onChange({ ...content, skills })}
          />
        )}
        {module !== 'basic' && module !== 'skills' && (
          <EntryListForm
            key={module}
            group={module}
            entries={content[module]}
            onChange={(entries) => onChange({ ...content, [module]: entries })}
          />
        )}
      </div>
      <div className="editor-form-footer">
        <Cloud size={14} />
        内容仅你可见，修改后自动保存
      </div>
    </section>
  );
}
