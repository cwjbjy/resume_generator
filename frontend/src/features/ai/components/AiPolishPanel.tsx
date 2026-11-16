import { useState, type FormEvent } from 'react';
import { Button } from '../../../components/ui/Button';
import { ErrorBox } from '../../../components/ui/ErrorBox';
import type { Resume } from '../../../types';
import { useAiPolish } from '../hooks/useAiPolish';
import type { PolishModule } from '../model/types';

export function AiPolishPanel({
  resume,
  save,
  onChange,
  available,
}: {
  resume: Resume;
  save: () => Promise<Resume>;
  onChange: (resume: Resume) => void;
  available: boolean;
}) {
  const [module, setModule] = useState<PolishModule>('work');
  const [selectedId, setSelectedId] = useState('');
  const [instruction, setInstruction] = useState('');
  const polish = useAiPolish(resume, save, onChange);
  const entries = module === 'basic' ? [] : resume.content[module];
  const entryId = entries.some((entry) => entry.id === selectedId) ? selectedId : entries[0]?.id;
  function submit(event: FormEvent) {
    event.preventDefault();
    void polish.run({ module, entryId: module === 'basic' ? undefined : entryId }, instruction);
  }
  return (
    <div className="ai-panel">
      <form onSubmit={submit}>
        <label>
          润色模块
          <select
            value={module}
            disabled={polish.loading}
            onChange={(event) => setModule(event.target.value as PolishModule)}
          >
            <option value="work">工作经历</option>
            <option value="proj">项目经历</option>
            <option value="basic">个人简介</option>
          </select>
        </label>
        {module !== 'basic' && (
          <label>
            选择经历
            <select
              value={entryId ?? ''}
              disabled={polish.loading || entries.length === 0}
              onChange={(event) => setSelectedId(event.target.value)}
            >
              {entries.length === 0 && <option value="">请先添加一条经历</option>}
              {entries.map((entry, index) => (
                <option key={entry.id} value={entry.id}>
                  {entry.company || entry.name || `经历 ${index + 1}`}
                </option>
              ))}
            </select>
          </label>
        )}
        <label>
          润色要求（选填）
          <textarea
            rows={2}
            maxLength={1000}
            value={instruction}
            onChange={(event) => setInstruction(event.target.value)}
            placeholder="例如突出职责、梳理表达层次，保留真实经历"
          />
        </label>
        <Button
          type="submit"
          className="primary"
          busy={polish.loading}
          disabled={!available || (module !== 'basic' && !entryId)}
        >
          生成润色建议
        </Button>
      </form>
      {polish.error && !polish.loading && <ErrorBox error={polish.error} />}
      {polish.result && !polish.loading && (
        <section className="ai-result" aria-label="润色建议">
          <div className="ai-comparison">
            <label>
              原文
              <textarea rows={7} readOnly value={polish.result.original} />
            </label>
            <label>
              建议内容
              <textarea
                rows={7}
                value={polish.suggested}
                onChange={(event) => polish.setSuggested(event.target.value)}
              />
            </label>
          </div>
          {polish.result.notes.length > 0 && (
            <ul>
              {polish.result.notes.map((note, index) => (
                <li key={index}>{note}</li>
              ))}
            </ul>
          )}
          <Button className="primary" disabled={!polish.suggested.trim()} onClick={polish.apply}>
            采用这份建议
          </Button>
        </section>
      )}
    </div>
  );
}
