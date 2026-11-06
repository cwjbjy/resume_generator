import { Loader2 } from 'lucide-react';
export function Loading() {
  return (
    <div className="loading">
      <Loader2 className="spin" size={24} />
      正在加载…
    </div>
  );
}
