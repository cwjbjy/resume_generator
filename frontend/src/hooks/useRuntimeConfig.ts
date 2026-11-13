import { useRequest } from 'ahooks';
import { api } from '../lib/http';
export function useRuntimeConfig() {
  return useRequest(() => api<{ mockEnabled: boolean }>('GET', '/config'), {
    cacheKey: 'config',
    retryCount: 1,
  });
}
