import { useRequest } from 'ahooks';
import { runtimeConfigApi } from '../app/api/runtimeConfigApi';
export function useRuntimeConfig() {
  return useRequest(runtimeConfigApi.get, {
    cacheKey: 'config',
    retryCount: 1,
  });
}
