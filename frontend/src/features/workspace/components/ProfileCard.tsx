import { ChevronRight, Crown, Pencil } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Button } from '../../../components/ui/Button';
import { type User } from '../../../types/index';

export function ProfileCard({ user, onEdit }: { user: User | null; onEdit: () => void }) {
  return (
    <aside className="profile-card">
      <div className="profile-avatar">{(user?.nickname || user?.username || '我').slice(0, 1)}</div>
      <h3>{user?.nickname || user?.username}</h3>
      <span className={`identity ${user?.member ? 'member' : ''}`}>
        {user?.member ? (
          <>
            <Crown size={13} />
            工坊会员
          </>
        ) : (
          '免费用户'
        )}
      </span>
      <p>
        {user?.member ? `有效期至 ${user.memberExpireAt?.slice(0, 10)}` : '每日可免费导出 1 次'}
      </p>
      <Button className="full" onClick={() => onEdit()}>
        <Pencil size={14} />
        编辑资料
      </Button>
      <Link className="profile-upgrade" to="/pricing">
        <Crown size={18} />
        {user?.member ? '管理会员权益' : '解锁更多可能'}
        <ChevronRight size={16} />
      </Link>
    </aside>
  );
}
