import { FileText } from 'lucide-react';
import { Link } from 'react-router-dom';
export function Brand() {
  return (
    <Link className="brand" to="/">
      <span className="brand-mark">
        <FileText size={23} strokeWidth={2.1} />
      </span>
      <span>
        简历工坊<small>RESUME CRAFT</small>
      </span>
    </Link>
  );
}
