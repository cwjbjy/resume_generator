import { api } from '../../../lib/http';
import type { ExportLog, PageResult } from '../../../types';

export const exportApi = {
  list(page = 1, size = 12) {
    return api<PageResult<ExportLog>>('GET', `/exports/logs?page=${page}&size=${size}`);
  },
  reserve(resumeId: number, resumeVersion?: number, token?: string) {
    return api<{ id: number }>('POST', '/exports', { resumeId, resumeVersion }, token);
  },
  complete(id: number, success: boolean, token?: string) {
    return api<{ status: string }>('POST', `/exports/${id}/complete`, { success }, token);
  },
};
