import { api } from '../../../lib/http';
import { useSession } from '../../../stores/sessionStore';
import { type User } from '../../../types';

export const userApi = {
  current() {
    return api<User>('GET', '/users/me');
  },
  updateProfile(profile: Pick<User, 'nickname' | 'email'>) {
    return api<User>('PUT', '/users/me', profile);
  },
};

let latestRefresh = 0;

export async function refreshUser() {
  const token = useSession.getState().token;
  const refreshVersion = ++latestRefresh;
  const user = await userApi.current();
  if (token && useSession.getState().token === token && refreshVersion === latestRefresh) {
    useSession.getState().update(user);
  }
  return user;
}
