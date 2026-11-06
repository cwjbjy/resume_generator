import { ExternalLink, Loader2 } from 'lucide-react';
import { renderPdfPages } from './renderPdfPages';

import { useEffect, useRef, useState } from 'react';
export function PdfPages({ url }: { url: string }) {
  const pages = useRef<HTMLDivElement>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [count, setCount] = useState(0);
  useEffect(
    () =>
      renderPdfPages(url, pages.current!, {
        onPageCount: setCount,
        onPageRendered: () => setLoading(false),
        onError: (error) => {
          setError(error.message);
          setLoading(false);
        },
      }),
    [url],
  );
  return (
    <div className="pdf-viewer" aria-label="PDF 文件预览">
      <div className="pdf-viewer-toolbar">
        <span>PDF 预览 {count ? `· 共 ${count} 页` : ''}</span>
        <a href={url} target="_blank" rel="noreferrer">
          在新窗口打开 <ExternalLink size={12} />
        </a>
      </div>
      {loading && (
        <div className="pdf-loading">
          <Loader2 className="spin" size={23} />
          正在渲染 PDF 页面…
        </div>
      )}
      {error && (
        <div className="pdf-fallback">
          <p>页面预览加载失败，可以直接下载 PDF，或在新窗口打开。</p>
          <iframe title="浏览器 PDF 备用预览" className="pdf-frame" src={url} />
        </div>
      )}
      <div className="pdf-rendered-pages" ref={pages} />
    </div>
  );
}
