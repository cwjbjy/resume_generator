import { Button } from '../../../components/ui/Button';
import { Modal } from '../../../components/ui/Modal';

export function RenameResumeDialog({
  title,
  busy,
  onClose,
  onSubmit,
}: {
  title: string;
  busy: boolean;
  onClose: () => void;
  onSubmit: (title: string) => void;
}) {
  return (
    <Modal title="重命名简历" onClose={onClose}>
      <form
        onSubmit={(event) => {
          event.preventDefault();
          onSubmit(String(new FormData(event.currentTarget).get('title')));
        }}
      >
        <label>
          简历标题
          <input name="title" defaultValue={title} maxLength={100} required />
        </label>
        <Button className="primary full" type="submit" busy={busy}>
          保存标题
        </Button>
      </form>
    </Modal>
  );
}
