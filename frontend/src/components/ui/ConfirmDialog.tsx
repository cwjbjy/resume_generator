import type { ReactNode } from 'react';
import { Button } from './Button';
import { Modal } from './Modal';

interface ConfirmDialogProps {
  title: string;
  children: ReactNode;
  confirmLabel: string;
  busy: boolean;
  danger?: boolean;
  onClose: () => void;
  onConfirm: () => void;
}
export function ConfirmDialog({
  title,
  children,
  confirmLabel,
  busy,
  danger = false,
  onClose,
  onConfirm,
}: ConfirmDialogProps) {
  return (
    <Modal title={title} onClose={onClose}>
      <p className="modal-description">{children}</p>
      <div className="modal-actions">
        <Button onClick={onClose}>取消</Button>
        <Button className={danger ? 'danger-fill' : 'primary'} busy={busy} onClick={onConfirm}>
          {confirmLabel}
        </Button>
      </div>
    </Modal>
  );
}
