import { Brand } from './Brand';
const copyrightYear = new Date().getFullYear();
export function SiteFooter() {
  return (
    <footer className="site-footer">
      <Brand />
      <p>认真对待每一份经历，让下一步更有底气。</p>
      <span>© {copyrightYear} 简历工坊 · 原创模板</span>
    </footer>
  );
}
