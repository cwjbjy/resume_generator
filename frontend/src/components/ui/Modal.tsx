import { useMount } from 'ahooks';
import { X } from 'lucide-react';
import { useRef, type ReactNode } from 'react';

interface ModalProps {
  title: string;
  children: ReactNode;
  onClose: () => void;
  wide?: boolean;
}
export function Modal({ title, children, onClose, wide = false }: ModalProps) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  useMount(() => {
    const dialog = dialogRef.current!;
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    dialog.setAttribute('closedby', 'any');
    dialog.showModal();
    return () => {
      dialog.close();
      document.body.style.overflow = previousOverflow;
    };
  });
  return (
    <dialog
      ref={dialogRef}
      className={`modal ${wide ? 'wide' : ''}`}
      aria-label={title}
      onCancel={(event) => {
        event.preventDefault();
        onClose();
      }}
    >
      <div className="modal-header">
        <h2>{title}</h2>
        <button type="button" className="icon-button" aria-label="关闭弹窗" onClick={onClose}>
          <X size={20} />
        </button>
      </div>
      {children}
    </dialog>
  );
}
