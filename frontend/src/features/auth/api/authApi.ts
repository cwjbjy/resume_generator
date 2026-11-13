import { api } from '../../../lib/http';
import type { User } from '../../../types';

export const authApi = {
  authenticate(credentials: Record<string, FormDataEntryValue>, register: boolean) {
    return api<{ token: string; user: User }>(
      'POST',
      register ? '/auth/register' : '/auth/login',
      credentials,
    );
  },
};
