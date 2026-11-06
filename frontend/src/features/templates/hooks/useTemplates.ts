import { useQuery } from '@tanstack/react-query';
import { api } from '../../../lib/http';
import { type Template } from '../../../types/index';
export function useTemplates() {
  return useQuery({
    queryKey: ['templates'],
    queryFn: () => api<Template[]>('GET', '/templates'),
    staleTime: 60000,
  });
}
