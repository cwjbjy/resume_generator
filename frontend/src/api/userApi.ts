import { api } from '../lib/http';
import { useSession } from '../stores/sessionStore';
import { type User } from '../types';

let latestRefresh = 0;

export async function refreshUser() {
  const token = useSession.getState().token;
  const refreshVersion = ++latestRefresh;
  const user = await api<User>('GET', '/users/me');
  if (token && useSession.getState().token === token && refreshVersion === latestRefresh) {
    useSession.getState().update(user);
  }
  return user;
}
