import { api } from '../../../lib/http';

export interface AuthenticationResult {
  token: string;
  id: number;
  username: string;
}

export const authApi = {
  logout(token: string) {
    return api<void>('POST', '/auth/logout', undefined, token);
  },
  authenticate(credentials: Record<string, FormDataEntryValue>, register: boolean) {
    return api<AuthenticationResult>(
      'POST',
      register ? '/auth/register' : '/auth/login',
      credentials,
    );
  },
};
