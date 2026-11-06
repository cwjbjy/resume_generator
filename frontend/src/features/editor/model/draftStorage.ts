import type { Content, Resume } from '../../../types';

export interface LocalDraft {
  title: string;
  templateId: number;
  content: Content;
  baseVersion: number;
  savedAt: number;
}

export function readLocalDraft(key: string, serverResume: Resume): LocalDraft | null {
  const raw = localStorage.getItem(key);
  if (!raw) return null;
  const draft = JSON.parse(raw) as LocalDraft;
  return draft.content && draft.savedAt > new Date(serverResume.updatedAt).getTime() ? draft : null;
}

export function validateDraft(resume: Resume) {
  if (!resume.title.trim()) return '请填写简历标题';
  const entries = [...resume.content.edu, ...resume.content.work, ...resume.content.proj];
  if (entries.some((entry) => entry.start && entry.end && entry.end < entry.start)) {
    return '结束时间不能早于开始时间';
  }
  return '';
}
