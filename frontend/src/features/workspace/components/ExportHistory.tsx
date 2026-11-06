import { Download } from 'lucide-react';
import type { ExportLog } from '../../../types';
export function ExportHistory({ records }: { records?: ExportLog[] }) {
  return (
    <div className="order-list">
      {records?.length === 0 && (
        <div className="empty-state">
          <Download size={32} />
          <h3>还没有导出记录</h3>
          <p>完成简历后，前往编辑器导出 PDF。</p>
        </div>
      )}
      {records?.map((e) => (
        <div className="order-row" key={e.id}>
          <Download size={22} />
          <div>
            <b>简历 #{e.resumeId}</b>
            <small>{new Date(e.createdAt).toLocaleString('zh-CN')}</small>
          </div>
          <span className="order-status">
            {e.status === 'COMPLETED'
              ? '导出成功'
              : e.status === 'PENDING'
                ? '生成中'
                : '未完成（不扣次数）'}
          </span>
        </div>
      ))}
    </div>
  );
}
