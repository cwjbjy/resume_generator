import type { Resume } from '../../../types';
import type { PolishResult, PolishTarget } from './types';

export function polishText(resume: Resume, target: PolishTarget): string {
  if (target.module === 'basic') return resume.content.basic.intro;
  const entry = resume.content[target.module].find((item) => item.id === target.entryId);
  if (!entry) throw new Error('这条经历已变更，请重新选择');
  return entry.desc ?? '';
}

export function applyPolish(resume: Resume, result: PolishResult, suggested: string): Resume {
  if (polishText(resume, result) !== result.original) {
    throw new Error('原文已发生更改，请重新生成建议后再采用');
  }
  if (!suggested.trim()) throw new Error('请保留需要采用的润色内容');
  if (result.module === 'basic') {
    return {
      ...resume,
      content: { ...resume.content, basic: { ...resume.content.basic, intro: suggested } },
    };
  }
  return {
    ...resume,
    content: {
      ...resume.content,
      [result.module]: resume.content[result.module].map((entry) =>
        entry.id === result.entryId ? { ...entry, desc: suggested } : entry,
      ),
    },
  };
}
