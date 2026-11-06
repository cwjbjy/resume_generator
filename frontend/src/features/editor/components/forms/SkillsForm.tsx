import { Plus, Tags, X } from 'lucide-react';
import { useState } from 'react';

export function SkillsForm({
  skills,
  onChange,
}: {
  skills: string[];
  onChange: (skills: string[]) => void;
}) {
  const [input, setInput] = useState('');
  function addSkill() {
    const value = input.trim();
    if (!value || skills.includes(value)) return;
    onChange([...skills, value]);
    setInput('');
  }
  return (
    <>
      <label>
        添加技能
        <div className="skill-input">
          <input
            aria-label="技能名称"
            placeholder="输入技能后按回车，例如 React"
            maxLength={50}
            value={input}
            onChange={(event) => setInput(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter' && !event.nativeEvent.isComposing) {
                event.preventDefault();
                addSkill();
              }
            }}
          />
          <button className="icon-button" aria-label="添加技能" onClick={addSkill}>
            <Plus size={18} />
          </button>
        </div>
      </label>
      <div className="skill-tags">
        {skills.map((skill, index) => (
          <span key={skill}>
            {skill}
            <button
              aria-label={`删除技能${skill}`}
              onClick={() => onChange(skills.filter((_, position) => position !== index))}
            >
              <X size={13} />
            </button>
          </span>
        ))}
      </div>
      <div className="editor-tip">
        <Tags size={18} />
        <span>建议选择 5–8 个与你目标岗位最相关的技能。</span>
      </div>
    </>
  );
}
