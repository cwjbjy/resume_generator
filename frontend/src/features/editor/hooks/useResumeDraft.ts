import { useDebounceEffect, useEventListener, useMemoizedFn, useMount, useRequest } from 'ahooks';
import { useRef, useState } from 'react';
import { resumeApi } from '../../resumes/api/resumeApi';
import { ApiError } from '../../../lib/http';
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
  const [conflicted, setConflicted] = useState(false);
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
  const conflict = useRef(false);

  useMount(() => {
    if (draftRecovery.readFailed) notify('本地草稿无法读取，已加载服务器版本');
  });

  function persistDraft(next: Resume): LocalDraft {
    const draft: LocalDraft = {
      title: next.title,
      templateId: next.templateId,
      content: next.content,
      baseVersion: version.current,
      savedAt: Date.now(),
      dirty: true,
    };
    try {
      localStorage.setItem(storageKey, JSON.stringify(draft));
    } catch {
      notify('浏览器本地存储已满，请及时保存简历');
    }
    return draft;
  }

  const change = useMemoizedFn((next: Resume) => {
    currentResume.current = next;
    revision.current += 1;
    setResume(next);
    if (!conflict.current) {
      setStatus('有未保存的更改');
      setError('');
    }
    persistDraft(next);
  });

  const flush = useMemoizedFn(async () => {
    if (localDraft) throw new Error('请先处理本地草稿');
    if (conflict.current) throw new ApiError('请先处理服务器版本冲突', 409, 409);
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
            try {
              if (!readLocalDraft(storageKey, saved)) localStorage.removeItem(storageKey);
            } catch {
              // 服务器已保存成功，本地存储故障保留到下次读取时按内容去重。
            }
            setStatus(
              `已保存 ${new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`,
            );
            setError('');
          } else {
            persistDraft(currentResume.current);
          }
        } catch (saveError) {
          if (saveError instanceof ApiError && saveError.status === 409) {
            conflict.current = true;
            setConflicted(true);
            setStatus('服务器版本冲突 · 自动保存已暂停');
          } else {
            setStatus('保存失败 · 本地草稿已保留');
          }
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
      if (localDraft || conflict.current || revision.current === savedRevision.current) return;
      void flush().catch(() => {});
    },
    [resume, localDraft, conflicted],
    { wait: 800 },
  );

  useEventListener('beforeunload', (event) => {
    if (localDraft || revision.current > savedRevision.current) {
      event.preventDefault();
      event.returnValue = '';
    }
  });
  useEventListener('online', () => {
    if (!conflict.current) void flush().catch(() => {});
  });

  const { run: loadServerVersion, loading: loadingServer } = useRequest(
    () => resumeApi.get(initialResume.id),
    {
      manual: true,
      onSuccess: (serverResume) => {
        const retainedDraft = persistDraft(currentResume.current);
        currentResume.current = serverResume;
        revision.current = 0;
        savedRevision.current = 0;
        version.current = serverResume.version;
        conflict.current = false;
        setResume(serverResume);
        setServerVersion(serverResume.version);
        setConflicted(false);
        setError('');
        setStatus('已加载服务器版本 · 本地草稿已保留');
        setDraftRecovery({ draft: retainedDraft, readFailed: false });
      },
      onError: (loadError) => notify(loadError.message),
    },
  );

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
    try {
      localStorage.removeItem(storageKey);
    } catch {
      notify('本地草稿清理失败，请检查浏览器存储权限');
      return;
    }
    setStatus('已加载服务器版本');
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
    conflicted,
    loadServerVersion,
    loadingServer,
    localDraft,
    restoreDraft,
    discardDraft,
  };
}
