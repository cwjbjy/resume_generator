import { useEventListener, useRequest } from 'ahooks';
import { refreshUser } from '../features/user/api/userApi';
import { useSession } from '../stores/sessionStore';

export function useCurrentUser() {
  const { token, user } = useSession();
  useRequest(refreshUser, {
    ready: Boolean(token),
    refreshDeps: [token],
    refreshOnWindowFocus: Boolean(token),
    focusTimespan: 30000,
    retryCount: 1,
  });
  useEventListener('storage', (event) => {
    if (event.key === 'rc-session' || event.key === null) useSession.getState().sync();
  });
  return user;
}
