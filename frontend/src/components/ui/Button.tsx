import { Loader2 } from 'lucide-react';
export function Button({
  children,
  busy = false,
  className = '',
  ...props
}: React.ButtonHTMLAttributes<HTMLButtonElement> & { busy?: boolean }) {
  return (
    <button {...props} disabled={busy || props.disabled} className={`button ${className}`}>
      {busy && <Loader2 size={16} className="spin" />}
      {children}
    </button>
  );
}
