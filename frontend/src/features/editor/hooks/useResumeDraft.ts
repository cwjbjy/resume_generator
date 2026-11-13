import { useDebounceEffect, useEventListener, useMemoizedFn, useMount } from 'ahooks';
import { useRef, useState } from 'react';
import { resumeApi } from '../../../api/resumeApi';
import { notify } from '../../../stores/noticeStore';
import type { Resume } from '../../../types';
import { readLocalDraft, validateDraft, type LocalDraft } from '../model/draftStorage';

interface DraftRecovery {
  draft: LocalDraft | null;
  readFailed: boolean;
}

export function useResumeDraft(initialResume: Resume, userId: number) {
  const storageKey = `rc-draft:${userId}:${initialResume.id}`;
  const [resume, setResume] = useState(initialResume);
  const [status, setStatus] = useState('已保存');
  const [error, setError] = useState('');
  const [serverVersion, setServerVersion] = useState(initialResume.version);
  const [draftRecovery, setDraftRecovery] = useState<DraftRecovery>(() => {
    try {
      return { draft: readLocalDraft(storageKey, initialResume), readFailed: false };
    } catch {
      return { draft: null, readFailed: true };
    }
  });
  const localDraft = draftRecovery.draft;
  const currentResume = useRef(initialResume);
  const revision = useRef(0);
  const savedRevision = useRef(0);
  const version = useRef(initialResume.version);
  const pendingSave = useRef<Promise<void> | null>(null);

  useMount(() => {
    if (draftRecovery.readFailed) notify('本地草稿无法读取，已加载服务器版本');
  });

  const change = useMemoizedFn((next: Resume) => {
    currentResume.current = next;
    revision.current += 1;
    setResume(next);
    setStatus('有未保存的更改');
    setError('');
    try {
      const draft: LocalDraft = {
        title: next.title,
        templateId: next.templateId,
        content: next.content,
        baseVersion: version.current,
        savedAt: Date.now(),
      };
      localStorage.setItem(storageKey, JSON.stringify(draft));
    } catch {
      notify('浏览器本地存储已满，请及时保存简历');
    }
  });

  const flush = useMemoizedFn(async () => {
    if (pendingSave.current) return pendingSave.current;
    async function saveChanges() {
      while (savedRevision.current < revision.current) {
        const snapshot = currentResume.current;
        const snapshotRevision = revision.current;
        const validationError = validateDraft(snapshot);
        if (validationError) {
          setError(validationError);
          throw new Error(validationError);
        }
        setStatus('保存中…');
        try {
          const saved = await resumeApi.save({ ...snapshot, version: version.current });
          version.current = saved.version;
          savedRevision.current = snapshotRevision;
          setServerVersion(saved.version);
          if (snapshotRevision === revision.current) {
            localStorage.removeItem(storageKey);
            setStatus(
              `已保存 ${new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`,
            );
            setError('');
          }
        } catch (saveError) {
          setStatus('保存失败 · 本地草稿已保留');
          setError((saveError as Error).message);
          throw saveError;
        }
      }
    }
    pendingSave.current = saveChanges();
    try {
      await pendingSave.current;
    } finally {
      pendingSave.current = null;
    }
  });

  const save = useMemoizedFn(async () => {
    await flush();
    return { ...currentResume.current, version: version.current };
  });

  useDebounceEffect(
    () => {
      if (localDraft || revision.current === savedRevision.current) return;
      void flush().catch(() => {});
    },
    [resume, localDraft],
    { wait: 800 },
  );

  useEventListener('beforeunload', (event) => {
    if (revision.current > savedRevision.current) {
      event.preventDefault();
      event.returnValue = '';
    }
  });
  useEventListener('online', () => void flush().catch(() => {}));

  function restoreDraft() {
    if (!localDraft) return;
    change({
      ...currentResume.current,
      title: localDraft.title,
      templateId: localDraft.templateId,
      content: localDraft.content,
    });
    dismissDraft();
  }

  function discardDraft() {
    localStorage.removeItem(storageKey);
    dismissDraft();
  }

  function dismissDraft() {
    setDraftRecovery({ draft: null, readFailed: false });
  }

  return {
    resume,
    change,
    save,
    status,
    error,
    serverVersion,
    localDraft,
    restoreDraft,
    discardDraft,
    dismissDraft,
  };
}
