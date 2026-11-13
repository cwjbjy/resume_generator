import { useRequest } from 'ahooks';
import { api } from '../../../lib/http';
import { type Template } from '../../../types/index';
export function useTemplates() {
  return useRequest(() => api<Template[]>('GET', '/templates'), {
    cacheKey: 'templates',
    staleTime: 60000,
    retryCount: 1,
  });
}
