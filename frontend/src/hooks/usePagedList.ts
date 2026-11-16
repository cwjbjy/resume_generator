import { usePagination, useUpdateEffect } from 'ahooks';
import type { PageResult } from '../types';

export function usePagedList<T>(
  service: (page: number, size: number) => Promise<PageResult<T>>,
  ready = true,
) {
  const request = usePagination(
    async ({ current, pageSize }) => {
      const page = await service(current, pageSize);
      return { total: page.total, list: page.records };
    },
    { defaultPageSize: 12, ready, retryCount: 1 },
  );
  const { data, pagination } = request;
  useUpdateEffect(() => {
    const lastPage = Math.max(1, pagination.totalPage);
    if (data && pagination.current > lastPage) pagination.changeCurrent(lastPage);
  }, [data, pagination.current, pagination.totalPage]);
  return request;
}
