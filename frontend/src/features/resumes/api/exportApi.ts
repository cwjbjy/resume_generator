import { api } from '../../../lib/http';
import type { ExportLog } from '../../../types';

export const exportApi = {
  list() {
    return api<ExportLog[]>('GET', '/exports/logs');
  },
  reserve(resumeId: number) {
    return api<{ id: number }>('POST', '/exports', { resumeId });
  },
  complete(id: number, success: boolean) {
    return api<{ status: string }>('POST', `/exports/${id}/complete`, { success });
  },
};
