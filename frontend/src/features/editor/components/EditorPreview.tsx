import { Minus, Plus, RotateCcw } from 'lucide-react';
import { useState } from 'react';
import { ResumePreview } from '../../resumes/components/ResumePreview';
import type { Content, Template } from '../../../types';

export function EditorPreview({
  content,
  template,
  watermark,
}: {
  content: Content;
  template: Template;
  watermark: boolean;
}) {
  const [zoom, setZoom] = useState(85);
  return (
    <section className="editor-preview">
      <div className="preview-toolbar">
        <div>
          <span className="live-dot" />
          实时预览 <span className="preview-template">/ {template.name}</span>
        </div>
        <div className="zoom-controls">
          <button aria-label="缩小预览" onClick={() => setZoom((value) => Math.max(50, value - 5))}>
            <Minus size={14} />
          </button>
          <span>{zoom}%</span>
          <button
            aria-label="放大预览"
            onClick={() => setZoom((value) => Math.min(100, value + 5))}
          >
            <Plus size={14} />
          </button>
          <span className="zoom-separator" />
          <button aria-label="重置缩放" onClick={() => setZoom(85)}>
            <RotateCcw size={14} />
          </button>
        </div>
      </div>
      <div className="preview-canvas">
        <div className="preview-page-holder" style={{ zoom: zoom / 100 }}>
          <ResumePreview content={content} template={template} watermark={watermark} />
        </div>
        <p className="preview-footnote">A4 标准尺寸 · PDF 导出时根据内容自动分页</p>
      </div>
    </section>
  );
}
