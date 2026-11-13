import { api } from '../../../lib/http';
import { type Resume } from '../../../types';
import { normalize } from '../model/normalizeResume';
type ResumeResponse = Omit<Resume, 'content'> & { content: Resume['content'] | string };

export const resumeApi = {
  async list() {
    return (await api<ResumeResponse[]>('GET', '/resumes')).map(normalize);
  },
  async get(id: string | number) {
    return normalize(await api<ResumeResponse>('GET', `/resumes/${id}`));
  },
  async create(templateId: number) {
    return normalize(await api<ResumeResponse>('POST', '/resumes', { templateId }));
  },
  async save(resume: Resume) {
    return normalize(await api<ResumeResponse>('PUT', `/resumes/${resume.id}`, resume));
  },
  duplicate(id: number) {
    return api('POST', `/resumes/${id}/duplicate`);
  },
  remove(id: number) {
    return api('DELETE', `/resumes/${id}`);
  },
};
