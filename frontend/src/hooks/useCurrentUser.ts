import { useRequest } from 'ahooks';
import { refreshUser } from '../api/userApi';
import { useSession } from '../stores/sessionStore';

export function useCurrentUser() {
  const { token, user } = useSession();
  useRequest(refreshUser, {
    ready: Boolean(token),
    refreshDeps: [token],
    refreshOnWindowFocus: true,
    focusTimespan: 30000,
    retryCount: 1,
  });
  return user;
}
