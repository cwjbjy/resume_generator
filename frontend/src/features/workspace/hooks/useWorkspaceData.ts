import { useRequest } from 'ahooks';
import { useRuntimeConfig } from '../../../hooks/useRuntimeConfig';
import { membershipApi } from '../../membership/api/membershipApi';
import { exportApi } from '../../resumes/api/exportApi';
import { resumeApi } from '../../resumes/api/resumeApi';
import { useTemplates } from '../../templates/hooks/useTemplates';

export type WorkspaceTab = 'resumes' | 'orders' | 'exports';
export function useWorkspaceData(tab: WorkspaceTab) {
  const resumes = useRequest(resumeApi.list, { retryCount: 1 });
  const orders = useRequest(membershipApi.listOrders, {
    ready: tab === 'orders',
    retryCount: 1,
  });
  const exports = useRequest(exportApi.list, {
    ready: tab === 'exports',
    retryCount: 1,
  });
  const templates = useTemplates();
  const config = useRuntimeConfig();
  return { resumes, orders, exports, templates, config };
}
