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
          ' 服务器版本已有更新。选择更新服务器会将这份草稿保存到当前服务器版本，请先确认内容。'}
      </p>
      <div className="modal-actions">
        <Button onClick={onDiscard}>使用服务器版本</Button>
        <Button className="primary" onClick={onRestore}>
          {draft.baseVersion === serverVersion ? '恢复本地草稿' : '用草稿更新服务器'}
        </Button>
      </div>
    </Modal>
  );
}
