import { ArrowUpRight, Crown, LogOut, Menu } from 'lucide-react';
import { useState } from 'react';
import { Link, NavLink, useLocation } from 'react-router-dom';
import { useCurrentUser } from '../../hooks/useCurrentUser';
import { notify } from '../../stores/noticeStore';
import { useSession } from '../../stores/sessionStore';
import { Brand } from './Brand';

export function SiteHeader() {
  const user = useCurrentUser();
  const logout = useSession((state) => state.logout);
  const location = useLocation();
  const [expandedPath, setExpandedPath] = useState<string | null>(null);
  const open = expandedPath === location.pathname;
  return (
    <header className="site-header">
      <div className="header-inner">
        <Brand />
        <nav className={open ? 'open' : ''}>
          <NavLink to="/templates" onClick={() => setExpandedPath(null)}>
            简历模板
          </NavLink>
          <NavLink to="/me" onClick={() => setExpandedPath(null)}>
            我的简历
          </NavLink>
          <NavLink to="/pricing" onClick={() => setExpandedPath(null)}>
            会员权益 <Crown size={13} />
          </NavLink>
        </nav>
        <div className="header-actions">
          {user ? (
            <>
              <Link to="/me" className="user-pill">
                <span className="avatar">{(user.nickname || user.username).slice(0, 1)}</span>
                {user.nickname || user.username}
                {user.member && <Crown size={15} className="gold" />}
              </Link>
              <button
                className="icon-button"
                aria-label="退出登录"
                onClick={() => {
                  logout();
                  notify('已退出登录');
                }}
              >
                <LogOut size={17} />
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="login-link">
                登录
              </Link>
              <Link className="button primary compact" to="/register">
                免费开始 <ArrowUpRight size={15} />
              </Link>
            </>
          )}
          <button
            className="icon-button mobile-menu"
            aria-label="展开导航"
            aria-expanded={open}
            onClick={() => setExpandedPath(open ? null : location.pathname)}
          >
            <Menu size={21} />
          </button>
        </div>
      </div>
    </header>
  );
}
