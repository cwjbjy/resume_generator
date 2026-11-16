import { useState } from 'react';
import { Modal } from '../../../components/ui/Modal';
import { useSession } from '../../../stores/sessionStore';
import type { Resume } from '../../../types';
import { useAiStatus } from '../hooks/useAiStatus';
import { AiMatchPanel } from './AiMatchPanel';
import { AiPolishPanel } from './AiPolishPanel';
import { AiServiceNotice } from './AiServiceNotice';

export function ResumeAiDialog({
  resume,
  save,
  onChange,
  onClose,
}: {
  resume: Resume;
  save: () => Promise<Resume>;
  onChange: (resume: Resume) => void;
  onClose: () => void;
}) {
  const [tab, setTab] = useState<'polish' | 'match'>('polish');
  const token = useSession((session) => session.token);
  const status = useAiStatus();
  return (
    <Modal title="AI 简历助手" wide onClose={onClose}>
      <AiServiceNotice {...status} />
      <div className="ai-tabs" aria-label="AI 功能">
        <button type="button" aria-pressed={tab === 'polish'} onClick={() => setTab('polish')}>
          内容润色
        </button>
        <button type="button" aria-pressed={tab === 'match'} onClick={() => setTab('match')}>
          岗位匹配
        </button>
      </div>
      {tab === 'polish' ? (
        <AiPolishPanel
          key={`${token}:${resume.id}`}
          resume={resume}
          save={save}
          onChange={onChange}
          available={Boolean(status.data?.available)}
        />
      ) : (
        <AiMatchPanel
          key={`${token}:${resume.id}`}
          save={save}
          available={Boolean(status.data?.available)}
        />
      )}
    </Modal>
  );
}
