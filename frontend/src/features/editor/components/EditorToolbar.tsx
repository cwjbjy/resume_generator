import {
  ArrowLeft,
  Cloud,
  CloudOff,
  Download,
  FileText,
  LayoutTemplate,
  Save,
  Sparkles,
} from 'lucide-react';
import { Button } from '../../../components/ui/Button';

interface EditorToolbarProps {
  title: string;
  status: string;
  error: string;
  exporting: boolean;
  onTitleChange: (title: string) => void;
  onBack: () => void;
  onSave: () => void;
  onSwitchTemplate: () => void;
  onExport: () => void;
  onAiAssist: () => void;
}
export function EditorToolbar({
  title,
  status,
  error,
  exporting,
  onTitleChange,
  onBack,
  onSave,
  onSwitchTemplate,
  onExport,
  onAiAssist,
}: EditorToolbarProps) {
  return (
    <div className="editor-toolbar">
      <div className="editor-title">
        <button className="icon-button" aria-label="返回我的简历" onClick={onBack}>
          <ArrowLeft size={19} />
        </button>
        <span className="toolbar-separator" />
        <FileText size={19} />
        <input
          aria-label="简历标题"
          value={title}
          maxLength={100}
          onChange={(event) => onTitleChange(event.target.value)}
        />
        <span className={`save-status ${error ? 'failed' : ''}`}>
          {error ? <CloudOff size={14} /> : <Cloud size={14} />}
          {status}
        </span>
      </div>
      <div className="editor-toolbar-actions">
        <Button className="small" onClick={onAiAssist}>
          <Sparkles size={15} />
          AI 助手
        </Button>
        <Button className="small" onClick={onSave}>
          <Save size={15} />
          保存
        </Button>
        <Button className="small" onClick={onSwitchTemplate}>
          <LayoutTemplate size={15} />
          更换模板
        </Button>
        <Button className="primary small" busy={exporting} onClick={onExport}>
          <Download size={15} />
          {exporting ? '生成 PDF 中…' : '导出 PDF'}
        </Button>
      </div>
    </div>
  );
}
