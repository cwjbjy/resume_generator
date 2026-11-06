import type { Content, Entry } from '../../../types';
export function sortedEducation(entries: Entry[]) {
  return [...entries].sort((a, b) => (b.end || '9999').localeCompare(a.end || '9999'));
}
export function sections(
  content: Content,
): { key: string; title: string; text?: string; entries?: Entry[]; skills?: string[] }[] {
  return [
    { key: 'intro', title: '个人简介', text: content.basic.intro },
    { key: 'edu', title: '教育经历', entries: sortedEducation(content.edu) },
    { key: 'work', title: '工作经历', entries: content.work },
    { key: 'proj', title: '项目经历', entries: content.proj },
    { key: 'skills', title: '专业技能', skills: content.skills },
    ...content.custom.map((entry) => ({
      key: entry.id,
      title: entry.title || '自定义模块',
      text: entry.desc,
    })),
  ];
}
