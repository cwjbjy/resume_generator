import { usePagedList } from '../../../hooks/usePagedList';
import { useRuntimeConfig } from '../../../hooks/useRuntimeConfig';
import { membershipApi } from '../../membership/api/membershipApi';
import { exportApi } from '../../resumes/api/exportApi';
import { resumeApi } from '../../resumes/api/resumeApi';
import { useTemplates } from '../../templates/hooks/useTemplates';

export type WorkspaceTab = 'resumes' | 'orders' | 'exports';
export function useWorkspaceData(tab: WorkspaceTab) {
  const resumes = usePagedList(resumeApi.list);
  const orders = usePagedList(membershipApi.listOrders, tab === 'orders');
  const exports = usePagedList(exportApi.list, tab === 'exports');
  const templates = useTemplates();
  const config = useRuntimeConfig();
  return { resumes, orders, exports, templates, config };
}
