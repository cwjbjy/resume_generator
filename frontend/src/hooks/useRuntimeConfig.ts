import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/http';
export function useRuntimeConfig() {
  return useQuery({
    queryKey: ['config'],
    queryFn: () => api<{ mockEnabled: boolean }>('GET', '/config'),
  });
}
