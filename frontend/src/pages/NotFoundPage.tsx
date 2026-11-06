import { FileText } from 'lucide-react';
import { Link } from 'react-router-dom';
export function NotFoundPage() {
  return (
    <main className="empty-state not-found">
      <FileText size={46} />
      <h1>这一页还没有故事</h1>
      <p>你访问的页面不存在，回到模板中心继续吧。</p>
      <Link className="button primary" to="/templates">
        返回模板中心
      </Link>
    </main>
  );
}
