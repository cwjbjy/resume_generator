import { useRequest } from 'ahooks';
import { templateApi } from '../api/templateApi';
export function useTemplates() {
  return useRequest(templateApi.list, {
    cacheKey: 'templates',
    staleTime: 60000,
    retryCount: 1,
  });
}
