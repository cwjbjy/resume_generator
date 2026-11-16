import { api } from '../../../lib/http';

export interface AuthenticationResult {
  token: string;
  id: number;
  username: string;
}

export const authApi = {
  authenticate(credentials: Record<string, FormDataEntryValue>, register: boolean) {
    return api<AuthenticationResult>(
      'POST',
      register ? '/auth/register' : '/auth/login',
      credentials,
    );
  },
};
