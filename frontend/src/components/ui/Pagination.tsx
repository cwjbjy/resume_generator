import { Button } from './Button';

interface PaginationProps {
  current: number;
  totalPage: number;
  total: number;
  loading: boolean;
  onChange: (page: number) => void;
}

export function Pagination({ current, totalPage, total, loading, onChange }: PaginationProps) {
  if (totalPage <= 1 && current === 1) return null;
  return (
    <nav className="pagination" aria-label="列表分页">
      <Button disabled={loading || current <= 1} onClick={() => onChange(current - 1)}>
        上一页
      </Button>
      <span aria-live="polite">
        第 {current} / {Math.max(1, totalPage)} 页，共 {total} 条
      </span>
      <Button disabled={loading || current >= totalPage} onClick={() => onChange(current + 1)}>
        下一页
      </Button>
    </nav>
  );
}
