import { api } from '../../../lib/http';
import { userApi } from '../../user/api/userApi';

export const authApi = {
  async authenticate(credentials: Record<string, FormDataEntryValue>, register: boolean) {
    const result = await api<{ token: string; id: number; username: string }>(
      'POST',
      register ? '/auth/register' : '/auth/login',
      credentials,
    );
    const user = await userApi.current(result.token);
    return { ...result, user };
  },
};
