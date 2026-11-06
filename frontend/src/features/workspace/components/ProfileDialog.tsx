import { Button } from '../../../components/ui/Button';
import { Modal } from '../../../components/ui/Modal';
import type { User } from '../../../types';

export interface ProfileInput {
  nickname: string;
  email: string;
}
export function ProfileDialog({
  user,
  busy,
  onClose,
  onSubmit,
}: {
  user: User;
  busy: boolean;
  onClose: () => void;
  onSubmit: (profile: ProfileInput) => void;
}) {
  return (
    <Modal title="编辑个人资料" onClose={onClose}>
      <form
        onSubmit={(event) => {
          event.preventDefault();
          const data = new FormData(event.currentTarget);
          onSubmit({ nickname: String(data.get('nickname')), email: String(data.get('email')) });
        }}
      >
        <label>
          昵称
          <input
            name="nickname"
            required
            maxLength={50}
            defaultValue={user.nickname || user.username}
          />
        </label>
        <label>
          邮箱
          <input type="email" name="email" maxLength={120} defaultValue={user.email} />
        </label>
        <Button type="submit" className="primary full" busy={busy}>
          保存资料
        </Button>
      </form>
    </Modal>
  );
}
