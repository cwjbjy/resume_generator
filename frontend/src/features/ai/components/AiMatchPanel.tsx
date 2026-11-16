import { useLockFn, useRequest, useUnmountedRef } from 'ahooks';
import { useState, type FormEvent } from 'react';
import { Button } from '../../../components/ui/Button';
import { ErrorBox } from '../../../components/ui/ErrorBox';
import { useSession } from '../../../stores/sessionStore';
import type { Resume } from '../../../types';
import { aiApi } from '../api/aiApi';
import { MatchAnalysis } from './MatchAnalysis';

export function AiMatchPanel({
  save,
  available,
}: {
  save: () => Promise<Resume>;
  available: boolean;
}) {
  const token = useSession((session) => session.token);
  const unmounted = useUnmountedRef();
  const [jobDescription, setJobDescription] = useState('');
  const request = useRequest(
    async () => {
      const requestToken = useSession.getState().token;
      const saved = await save();
      if (unmounted.current || useSession.getState().token !== requestToken)
        throw new Error('登录状态已变化，请重新操作');
      const result = await aiApi.match(saved.id, jobDescription.trim(), requestToken ?? undefined);
      return { result, token: requestToken };
    },
    { manual: true },
  );
  const analyze = useLockFn(() => request.runAsync().catch(() => {}));
  function submit(event: FormEvent) {
    event.preventDefault();
    void analyze();
  }
  const result = request.data?.token === token ? request.data.result : undefined;
  return (
    <div className="ai-panel">
      <form onSubmit={submit}>
        <label>
          目标岗位描述（JD）
          <textarea
            rows={7}
            maxLength={12000}
            required
            value={jobDescription}
            disabled={request.loading}
            onChange={(event) => setJobDescription(event.target.value)}
            placeholder="粘贴岗位职责与任职要求"
          />
        </label>
        <Button
          type="submit"
          className="primary"
          busy={request.loading}
          disabled={!available || !jobDescription.trim()}
        >
          分析岗位匹配度
        </Button>
      </form>
      {request.error && !request.loading && <ErrorBox error={request.error} />}
      {result && !request.loading && <MatchAnalysis result={result} />}
    </div>
  );
}
