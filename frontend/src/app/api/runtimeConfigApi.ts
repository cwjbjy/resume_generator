import { api } from '../../lib/http';

export const runtimeConfigApi = {
  get() {
    return api<{ mockEnabled: boolean }>('GET', '/config');
  },
};
