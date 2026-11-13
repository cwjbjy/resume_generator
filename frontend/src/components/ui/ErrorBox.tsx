import { Button } from './Button';
export function ErrorBox({
  error,
  retry,
}: {
  error: Error | null | undefined;
  retry?: () => void;
}) {
  return (
    <div className="error-box" role="alert">
      {error?.message || '加载失败'}
      {retry && <Button onClick={retry}>重新加载</Button>}
    </div>
  );
}
