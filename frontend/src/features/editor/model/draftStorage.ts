import type { Content, Resume } from '../../../types';

export interface LocalDraft {
  title: string;
  templateId: number;
  content: Content;
  baseVersion: number;
  savedAt: number;
  dirty?: boolean;
}

export function readLocalDraft(key: string, serverResume: Resume): LocalDraft | null {
  const raw = localStorage.getItem(key);
  if (!raw) return null;
  const draft = JSON.parse(raw) as LocalDraft;
  if (
    !draft ||
    typeof draft.title !== 'string' ||
    !Number.isInteger(draft.templateId) ||
    !Number.isInteger(draft.baseVersion) ||
    !Number.isFinite(draft.savedAt) ||
    !validContent(draft.content)
  ) {
    throw new Error('本地草稿格式无效');
  }
  if (draft.dirty === false) return null;
  const matchesServer =
    draft.title === serverResume.title &&
    draft.templateId === serverResume.templateId &&
    sameContent(draft.content, serverResume.content);
  return matchesServer ? null : draft;
}

function sameContent(first: unknown, second: unknown): boolean {
  if (first === second) return true;
  if (!first || !second || typeof first !== 'object' || typeof second !== 'object') return false;
  if (Array.isArray(first) !== Array.isArray(second)) return false;
  const firstRecord = first as Record<string, unknown>;
  const secondRecord = second as Record<string, unknown>;
  const keys = Object.keys(firstRecord);
  return (
    keys.length === Object.keys(secondRecord).length &&
    keys.every(
      (key) => Object.hasOwn(secondRecord, key) && sameContent(firstRecord[key], secondRecord[key]),
    )
  );
}

function validContent(content: Content | undefined): boolean {
  if (!content?.basic || typeof content.basic !== 'object') return false;
  const basicFields = ['name', 'job', 'phone', 'email', 'city', 'intro'] as const;
  if (!basicFields.every((field) => typeof content.basic[field] === 'string')) return false;
  if (
    !Array.isArray(content.skills) ||
    !content.skills.every((skill) => typeof skill === 'string')
  ) {
    return false;
  }
  return ['edu', 'work', 'proj', 'custom'].every((group) => {
    const entries = content[group as 'edu' | 'work' | 'proj' | 'custom'];
    return (
      Array.isArray(entries) &&
      entries.every(
        (entry) =>
          entry &&
          typeof entry === 'object' &&
          typeof entry.id === 'string' &&
          Object.values(entry).every((value) => typeof value === 'string'),
      )
    );
  });
}

export function validateDraft(resume: Resume) {
  if (!resume.title.trim()) return '请填写简历标题';
  const entries = [...resume.content.edu, ...resume.content.work, ...resume.content.proj];
  if (entries.some((entry) => entry.start && entry.end && entry.end < entry.start)) {
    return '结束时间不能早于开始时间';
  }
  return '';
}
