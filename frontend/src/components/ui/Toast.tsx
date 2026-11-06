import { CheckCircle2 } from 'lucide-react';
import { useNotice } from '../../stores/noticeStore';
export function Toast() {
  const text = useNotice((s) => s.text);
  return text ? (
    <output className="toast">
      <CheckCircle2 size={18} />
      {text}
    </output>
  ) : null;
}
