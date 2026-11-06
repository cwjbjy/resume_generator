import { Button } from '../../../components/ui/Button';
import { Modal } from '../../../components/ui/Modal';
import type { LocalDraft } from '../model/draftStorage';

interface DraftRecoveryDialogProps {
  draft: LocalDraft;
  serverVersion: number;
  onDismiss: () => void;
  onDiscard: () => void;
  onRestore: () => void;
}
export function DraftRecoveryDialog({
  draft,
  serverVersion,
  onDismiss,
  onDiscard,
  onRestore,
}: DraftRecoveryDialogProps) {
  return (
    <Modal title="发现未同步的本地草稿" onClose={onDismiss}>
      <p className="modal-description">
        检测到 {new Date(draft.savedAt).toLocaleString('zh-CN')}{' '}
        保存的本地草稿。可以恢复后继续编辑，或使用服务器版本。
        {draft.baseVersion !== serverVersion &&
          ' 服务器版本也有更新，恢复会以本地内容替换当前内容。'}
      </p>
      <div className="modal-actions">
        <Button onClick={onDiscard}>使用服务器版本</Button>
        <Button className="primary" onClick={onRestore}>
          恢复本地草稿
        </Button>
      </div>
    </Modal>
  );
}
