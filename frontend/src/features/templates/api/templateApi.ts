import { api } from '../../../lib/http';
import type { Template } from '../../../types';

export const templateApi = {
  list() {
    return api<Template[]>('GET', '/templates');
  },
  get(id: string | number | undefined) {
    return api<Template>('GET', `/templates/${id}`);
  },
};
