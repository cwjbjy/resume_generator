import axios from 'axios';
import { useSession } from '../stores/sessionStore';
const http = axios.create({ baseURL: '/api', timeout: 15000 });

http.interceptors.request.use((config) => {
  const token = useSession.getState().token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.message ||
      (error.code === 'ECONNABORTED'
        ? '请求超时，请稍后重试'
        : '暂时无法连接服务，请确认后端已启动');
    if (error.response?.status === 401 && !error.config?.url?.includes('/auth/')) {
      useSession.getState().logout();
      const redirect = encodeURIComponent(window.location.pathname + window.location.search);
      window.location.assign(`/login?redirect=${redirect}`);
    }
    return Promise.reject(new Error(message));
  },
);

export async function api<T>(method: string, url: string, data?: unknown): Promise<T> {
  const response = await http.request<{ data: T }>({ method, url, data });
  return response.data.data;
}
