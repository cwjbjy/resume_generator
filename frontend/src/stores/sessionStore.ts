import { clearCache } from 'ahooks';
import { create } from 'zustand';
import { type User } from '../types';
interface SessionState {
  token: string | null;
  user: User | null;
  login: (token: string, user: User) => void;
  update: (user: User) => void;
  logout: () => void;
}

function readSession(): Pick<SessionState, 'token' | 'user'> {
  try {
    const saved = JSON.parse(localStorage.getItem('rc-session') || '{}');
    return { token: saved.token || null, user: saved.user || null };
  } catch {
    return { token: null, user: null };
  }
}

export const useSession = create<SessionState>((set, get) => ({
  ...readSession(),
  login(token, user) {
    clearCache();
    localStorage.setItem('rc-session', JSON.stringify({ token, user }));
    set({ token, user });
  },
  update(user) {
    localStorage.setItem('rc-session', JSON.stringify({ token: get().token, user }));
    set({ user });
  },
  logout() {
    clearCache();
    localStorage.removeItem('rc-session');
    set({ token: null, user: null });
  },
}));
