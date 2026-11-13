import { useRequest } from 'ahooks';
import { resumeApi } from '../../../api/resumeApi';
import { useRuntimeConfig } from '../../../hooks/useRuntimeConfig';
import { api } from '../../../lib/http';
import type { ExportLog, Order } from '../../../types';
import { useTemplates } from '../../templates/hooks/useTemplates';

export type WorkspaceTab = 'resumes' | 'orders' | 'exports';
export function useWorkspaceData(tab: WorkspaceTab) {
  const resumes = useRequest(resumeApi.list, { retryCount: 1 });
  const orders = useRequest(() => api<Order[]>('GET', '/orders'), {
    ready: tab === 'orders',
    retryCount: 1,
  });
  const exports = useRequest(() => api<ExportLog[]>('GET', '/exports/logs'), {
    ready: tab === 'exports',
    retryCount: 1,
  });
  const templates = useTemplates();
  const config = useRuntimeConfig();
  return { resumes, orders, exports, templates, config };
}
