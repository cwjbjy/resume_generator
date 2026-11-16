import axios from 'axios';
import { useSession } from '../stores/sessionStore';

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status?: number,
    readonly code?: number | string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

const http = axios.create({ baseURL: '/api', timeout: 15000 });

http.interceptors.request.use((config) => {
  const token = useSession.getState().token;
  if (token && !config.headers.Authorization) config.headers.Authorization = `Bearer ${token}`;
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
    if (error.response?.status === 401) useSession.getState().sync();
    const token = useSession.getState().token;
    const requestAuthorization = error.config?.headers?.Authorization;
    if (
      error.response?.status === 401 &&
      !error.config?.url?.includes('/auth/') &&
      token &&
      requestAuthorization === `Bearer ${token}`
    ) {
      useSession.getState().logout();
      const redirect = encodeURIComponent(window.location.pathname + window.location.search);
      window.location.assign(`/login?redirect=${redirect}`);
    }
    return Promise.reject(
      new ApiError(message, error.response?.status, error.response?.data?.code ?? error.code),
    );
  },
);

export async function api<T>(
  method: string,
  url: string,
  data?: unknown,
  token?: string,
  timeout?: number,
): Promise<T> {
  const response = await http.request<{ data: T }>({
    method,
    url,
    data,
    timeout,
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data.data;
}
