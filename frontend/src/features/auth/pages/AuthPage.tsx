import { useBoolean, useLockFn, useRequest } from 'ahooks';
import { ArrowRight, ShieldCheck } from 'lucide-react';
import { type FormEvent, useRef } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { Button } from '../../../components/ui/Button';
import { notify } from '../../../stores/noticeStore';
import { useSession } from '../../../stores/sessionStore';
import { ApiError } from '../../../lib/http';
import { userApi } from '../../user/api/userApi';
import { authApi, type AuthenticationResult } from '../api/authApi';
import { AuthStory } from '../components/AuthStory';
export function AuthPage({ register = false }: { register?: boolean }) {
  const [params] = useSearchParams();
  const redirect = params.get('redirect') || '/me';
  const navigate = useNavigate();
  const login = useSession((s) => s.login);
  const pending = useRef<AuthenticationResult | null>(null);
  const [awaitingProfile, { setTrue: waitForProfile, setFalse: resetAuthentication }] =
    useBoolean(false);
  const authentication = useRequest(
    async (credentials: Record<string, FormDataEntryValue>) => {
      const result = pending.current ?? (await authApi.authenticate(credentials, register));
      pending.current = result;
      waitForProfile();
      const user = await userApi.current(result.token);
      return { ...result, user };
    },
    {
      manual: true,
      onError(error) {
        if (pending.current && error instanceof ApiError && error.status === 401) {
          pending.current = null;
          resetAuthentication();
          if (register) {
            notify('账号已创建，请登录后继续');
            navigate(`/login?redirect=${encodeURIComponent(redirect)}`, { replace: true });
          }
        }
      },
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
              disabled={awaitingProfile || authentication.loading}
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
                disabled={awaitingProfile || authentication.loading}
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
              disabled={awaitingProfile || authentication.loading}
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
              {awaitingProfile && (register ? '账号已创建，' : '认证已成功，')}
              {authentication.error.message}
            </div>
          )}
          <Button busy={authentication.loading} className="primary full" type="submit">
            {awaitingProfile ? '重试加载资料' : register ? '免费注册' : '登录'}{' '}
            <ArrowRight size={16} />
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
