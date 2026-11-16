import { useMount, useRequest } from 'ahooks';
import { refreshUser } from '../features/user/api/userApi';
import { useSession } from '../stores/sessionStore';

export function useCurrentUser() {
  const { token, user } = useSession();
  const { run } = useRequest(refreshUser, {
    manual: true,
    refreshOnWindowFocus: Boolean(token),
    focusTimespan: 30000,
    retryCount: 1,
  });
  useMount(() => {
    if (token) run();
  });
  return user;
}
