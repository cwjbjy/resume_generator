import { type Content, type Resume } from '../../../types';
export function normalize(resume: Omit<Resume, 'content'> & { content: Content | string }): Resume {
  return {
    ...resume,
    content: typeof resume.content === 'string' ? JSON.parse(resume.content) : resume.content,
  };
}
