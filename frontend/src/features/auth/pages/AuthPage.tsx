import { useLockFn, useRequest } from 'ahooks';
import { ArrowRight, ShieldCheck } from 'lucide-react';
import type { FormEvent } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { Button } from '../../../components/ui/Button';
import { api } from '../../../lib/http';
import { notify } from '../../../stores/noticeStore';
import { useSession } from '../../../stores/sessionStore';
import { type User } from '../../../types/index';
import { AuthStory } from '../components/AuthStory';
export function AuthPage({ register = false }: { register?: boolean }) {
  const [params] = useSearchParams();
  const redirect = params.get('redirect') || '/me';
  const navigate = useNavigate();
  const login = useSession((s) => s.login);
  const authentication = useRequest(
    (credentials: Record<string, FormDataEntryValue>) =>
      api<{ token: string; user: User }>(
        'POST',
        register ? '/auth/register' : '/auth/login',
        credentials,
      ),
    {
      manual: true,
      onSuccess(result) {
        login(result.token, result.user);
        navigate(redirect.startsWith('/') && !redirect.startsWith('//') ? redirect : '/me', {
          replace: true,
        });
        notify(register ? '注册成功，欢迎来到简历工坊' : '欢迎回来');
      },
    },
  );
  const authenticate = useLockFn(async (credentials: Record<string, FormDataEntryValue>) => {
    await authentication.runAsync(credentials).catch(() => {});
  });
  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    void authenticate(Object.fromEntries(new FormData(event.currentTarget)));
  }
  return (
    <main className="auth-page">
      <AuthStory />
      <div className="auth-form-wrap">
        <form onSubmit={submit} className="auth-form">
          <span className="eyebrow muted">
            {register ? 'START YOUR NEXT CHAPTER' : 'WELCOME BACK'}
          </span>
          <h2>{register ? '创建你的账号' : '欢迎回来'}</h2>
          <p>{register ? '开启一份更有说服力的自我介绍' : '继续打磨属于你的下一份机会'}</p>
          <label>
            用户名
            <input
              name="username"
              required
              autoComplete="username"
              pattern="[A-Za-z0-9_]{4,32}"
              title="4–32 位字母、数字或下划线"
              placeholder="输入用户名"
            />
          </label>
          {register && (
            <label>
              邮箱
              <input
                name="email"
                type="email"
                autoComplete="email"
                maxLength={120}
                placeholder="用于个人资料（选填）"
              />
            </label>
          )}
          <label>
            密码
            <input
              name="password"
              type="password"
              required
              minLength={6}
              maxLength={72}
              autoComplete={register ? 'new-password' : 'current-password'}
              placeholder="至少 6 位密码"
            />
          </label>
          {authentication.error && !authentication.loading && (
            <div className="inline-error" role="alert">
              {authentication.error.message}
            </div>
          )}
          <Button busy={authentication.loading} className="primary full" type="submit">
            {register ? '免费注册' : '登录'} <ArrowRight size={16} />
          </Button>
          <p className="auth-switch">
            {register ? '已经有账号？' : '还没有账号？'}
            <Link
              to={`${register ? '/login' : '/register'}?redirect=${encodeURIComponent(redirect)}`}
            >
              {register ? '立即登录' : '免费注册'}
            </Link>
          </p>
          <div className="auth-note">
            <ShieldCheck size={15} />
            简历内容私密保存，仅你本人可访问
          </div>
        </form>
      </div>
    </main>
  );
}
