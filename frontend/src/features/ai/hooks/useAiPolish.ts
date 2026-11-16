import { useLatest, useLockFn, useRequest, useUnmountedRef } from 'ahooks';
import { useState } from 'react';
import { notify } from '../../../stores/noticeStore';
import { useSession } from '../../../stores/sessionStore';
import type { Resume } from '../../../types';
import { aiApi } from '../api/aiApi';
import { applyPolish } from '../model/polishResume';
import type { PolishTarget } from '../model/types';

export function useAiPolish(
  resume: Resume,
  save: () => Promise<Resume>,
  onChange: (resume: Resume) => void,
) {
  const token = useSession((session) => session.token);
  const currentResume = useLatest(resume);
  const unmounted = useUnmountedRef();
  const [suggested, setSuggested] = useState('');
  const request = useRequest(
    async (target: PolishTarget, instruction: string) => {
      const requestToken = useSession.getState().token;
      const saved = await save();
      if (unmounted.current || useSession.getState().token !== requestToken)
        throw new Error('登录状态已变化，请重新操作');
      const result = await aiApi.polish(
        { ...target, resumeId: saved.id, instruction: instruction.trim() },
        requestToken ?? undefined,
      );
      return { result, token: requestToken, resumeId: saved.id };
    },
    {
      manual: true,
      onSuccess(response) {
        if (
          useSession.getState().token === response.token &&
          currentResume.current.id === response.resumeId
        ) {
          setSuggested(response.result.suggested);
        }
      },
    },
  );
  const result =
    request.data?.token === token && request.data.resumeId === resume.id
      ? request.data.result
      : undefined;
  const run = useLockFn(async (target: PolishTarget, instruction: string) => {
    await request.runAsync(target, instruction).catch(() => {});
  });
  function apply() {
    if (!result || useSession.getState().token !== token) return;
    try {
      onChange(applyPolish(currentResume.current, result, suggested));
      request.mutate(undefined);
      notify('润色内容已采用，正在保存');
    } catch (error) {
      notify((error as Error).message);
    }
  }
  return {
    result,
    suggested,
    setSuggested,
    loading: request.loading,
    error: request.error,
    run,
    apply,
  };
}
