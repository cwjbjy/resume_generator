import { useQuery } from '@tanstack/react-query';
import { resumeApi } from '../../../api/resumeApi';
import { useRuntimeConfig } from '../../../hooks/useRuntimeConfig';
import { api } from '../../../lib/http';
import type { ExportLog, Order } from '../../../types';
import { useTemplates } from '../../templates/hooks/useTemplates';

export type WorkspaceTab = 'resumes' | 'orders' | 'exports';
export function useWorkspaceData(tab: WorkspaceTab) {
  const resumes = useQuery({ queryKey: ['resumes'], queryFn: resumeApi.list });
  const orders = useQuery({
    queryKey: ['orders'],
    queryFn: () => api<Order[]>('GET', '/orders'),
    enabled: tab === 'orders',
  });
  const exports = useQuery({
    queryKey: ['exports'],
    queryFn: () => api<ExportLog[]>('GET', '/exports/logs'),
    enabled: tab === 'exports',
  });
  const templates = useTemplates();
  const config = useRuntimeConfig();
  return { resumes, orders, exports, templates, config };
}
