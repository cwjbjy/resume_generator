import { api } from '../lib/http';
import { useSession } from '../stores/sessionStore';
import { type User } from '../types';
export async function refreshUser() {
  const user = await api<User>('GET', '/users/me');
  useSession.getState().update(user);
  return user;
}
