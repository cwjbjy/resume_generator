import { ArrowRight, Crown } from 'lucide-react';
import { Link } from 'react-router-dom';

export function MembershipBanner() {
  return (
    <div className="membership-banner">
      <div className="banner-crown">
        <Crown size={26} />
      </div>
      <div>
        <h3>给下一次机会，多一点准备</h3>
        <p>解锁全部原创模板，无限次导出每一个更好的版本。</p>
      </div>
      <Link className="button dark" to="/pricing">
        了解会员权益 <ArrowRight size={16} />
      </Link>
    </div>
  );
}
