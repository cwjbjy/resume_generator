import { useQuery } from '@tanstack/react-query';
import { useEffect } from 'react';
import { api } from '../lib/http';
import { useSession } from '../stores/sessionStore';
import type { User } from '../types';

export function useCurrentUser() {
  const { token, user, update } = useSession();
  const { data } = useQuery({
    queryKey: ['user', token],
    queryFn: () => api<User>('GET', '/users/me'),
    enabled: Boolean(token),
    staleTime: 30000,
    refetchOnWindowFocus: true,
  });
  useEffect(() => {
    if (data) update(data);
  }, [data, update]);
  return user;
}
