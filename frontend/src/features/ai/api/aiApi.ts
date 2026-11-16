import { api } from '../../../lib/http';
import type { Content, Resume } from '../../../types';
import type {
  AiStatus,
  ChatMessage,
  ChatResult,
  ExportReview,
  MatchResult,
  PolishResult,
  PolishTarget,
} from '../model/types';

const aiTimeout = 210000;

export const aiApi = {
  status() {
    return api<AiStatus>('GET', '/ai/status');
  },
  polish(request: PolishTarget & { resumeId: number; instruction?: string }, token?: string) {
    return api<PolishResult>('POST', '/ai/polish', request, token, aiTimeout);
  },
  match(resumeId: number, jobDescription: string, token?: string) {
    return api<MatchResult>('POST', '/ai/match', { resumeId, jobDescription }, token, aiTimeout);
  },
  chat(messages: ChatMessage[], generate: boolean, token?: string) {
    return api<ChatResult>('POST', '/ai/chat', { messages, generate }, token, aiTimeout);
  },
  createResume(request: { templateId: number; title: string; content: Content }, token?: string) {
    return api<Resume>('POST', '/ai/resumes', request, token);
  },
  exportReview(exportId: number, token?: string) {
    return api<ExportReview>('POST', '/ai/export-review', { exportId }, token, aiTimeout);
  },
};
