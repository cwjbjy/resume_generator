import { useRequest } from 'ahooks';
import { aiApi } from '../api/aiApi';

export function useAiStatus() {
  return useRequest(aiApi.status);
}
