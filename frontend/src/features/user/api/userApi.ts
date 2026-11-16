import { api } from '../../../lib/http';
import { useSession } from '../../../stores/sessionStore';
import { type User } from '../../../types';

export const userApi = {
  current(token?: string) {
    return api<User>('GET', '/users/me', undefined, token);
  },
  updateProfile(profile: Pick<User, 'nickname' | 'email'>) {
    return api<User>('PUT', '/users/me', profile);
  },
};

let latestRefresh = 0;

export async function refreshUser() {
  const { token } = useSession.getState();
  const refreshVersion = ++latestRefresh;
  if (!token) return;
  const user = await userApi.current(token);
  if (useSession.getState().token === token && refreshVersion === latestRefresh) {
    useSession.getState().update(user);
  }
  return user;
}
