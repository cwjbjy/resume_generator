import { Check, Download } from 'lucide-react';
import { Modal } from '../../../components/ui/Modal';
import PdfViewer from '../../resumes/pdf/PdfViewer';
import { ExportAiReview } from '../../ai/components/ExportAiReview';

export function ExportDialog({
  url,
  filename,
  exportId,
  onClose,
}: {
  url: string;
  filename: string;
  exportId: number;
  onClose: () => void;
}) {
  return (
    <Modal title="简历已准备好，向机会出发" wide onClose={onClose}>
      <PdfViewer url={url} />
      <div className="pdf-actions">
        <span>
          <Check size={16} />
          已生成 A4 高清 PDF
        </span>
        <a className="button primary" href={url} download={filename}>
          <Download size={16} />
          下载 PDF
        </a>
      </div>
      <ExportAiReview key={exportId} exportId={exportId} />
    </Modal>
  );
}
