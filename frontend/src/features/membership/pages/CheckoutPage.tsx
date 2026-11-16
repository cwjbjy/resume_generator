import { useCountDown, useLatest, useLockFn, useRequest, useUnmountedRef } from 'ahooks';
import { CheckCircle2, ShieldCheck } from 'lucide-react';
import { useRef } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ErrorBox } from '../../../components/ui/ErrorBox';
import { Loading } from '../../../components/ui/Loading';
import { useRuntimeConfig } from '../../../hooks/useRuntimeConfig';
import { notify } from '../../../stores/noticeStore';
import { useSession } from '../../../stores/sessionStore';
import { refreshUser } from '../../user/api/userApi';
import { membershipApi } from '../api/membershipApi';
import { CheckoutActions } from '../components/CheckoutActions';
import { checkoutTitle } from '../utils/checkoutTitle';
import { statusText } from '../utils/orderStatus';
export function CheckoutPage() {
  const { id } = useParams();
  const token = useSession((session) => session.token);
  const currentContext = useLatest({ id, token });
  const unmounted = useUnmountedRef();
  // 会员资料刷新的状态机：每次只跟踪最近一个订单的刷新进度。
  // key = token:orderId，用于隔离不同账号/订单的轮询上下文。
  const refreshState = useRef<{ key: string | null; phase: 'idle' | 'refreshing' | 'done' }>({
    key: null,
    phase: 'idle',
  });
  const { data, error, loading, refreshAsync, cancel } = useRequest(
    async () => ({
      order: await membershipApi.getOrder(id, token ?? undefined),
      orderId: id,
      token,
    }),
    {
      refreshDeps: [id, token],
      retryCount: 1,
      pollingInterval: 3000,
      pollingWhenHidden: false,
      onSuccess(result) {
        // 三重上下文校验：轮询是长生命周期异步，迟到响应不得污染已切换的页面/会话。
        // currentContext 是渲染时快照，getState() 是实时读取，两者都查才能拦住窗口期内的变化。
        const isCurrent = () =>
          !unmounted.current &&
          currentContext.current.id === result.orderId &&
          currentContext.current.token === result.token &&
          useSession.getState().token === result.token;
        if (!isCurrent()) return;
        // 未支付：CREATED 继续轮询等待；进入其他终态则停止。
        if (result.order.status !== 'PAID') {
          if (result.order.status !== 'CREATED') cancel();
          return;
        }
        // 已支付：刷新会员资料后停止轮询。每个订单只刷新一次（done），
        // 刷新进行中不重复触发（refreshing）；key 隔离不同订单/账号的上下文。
        const key = `${result.token}:${result.order.id}`;
        const refresh = refreshState.current;
        if (refresh.key === key && refresh.phase === 'done') {
          cancel();
          return;
        }
        if (refresh.key === key && refresh.phase === 'refreshing') return;
        refresh.key = key;
        refresh.phase = 'refreshing';
        void refreshUser(isCurrent)
          .then((user) => {
            if (!user || !isCurrent()) return;
            if (refreshState.current.key === key) refreshState.current.phase = 'done';
            cancel();
          })
          .catch(() => {
            // Keep polling so a temporary profile failure can recover.
          })
          .finally(() => {
            // 仅当状态未被后续订单覆盖时复位，避免误清新订单的进度。
            if (refreshState.current.key === key && refreshState.current.phase === 'refreshing') {
              refreshState.current = { key: null, phase: 'idle' };
            }
          });
      },
    },
  );
  const order = data?.order.id === Number(id) && data.token === token ? data.order : undefined;
  const [remainingTime] = useCountDown({ targetDate: order?.expiredAt });
  const config = useRuntimeConfig();
  const payment = useRequest(
    async (success: boolean) => {
      const orderId = id;
      const requestToken = useSession.getState().token;
      await membershipApi.mockPayment(Number(orderId), success, requestToken ?? undefined);
      if (
        !unmounted.current &&
        currentContext.current.id === orderId &&
        useSession.getState().token === requestToken
      ) {
        await refreshAsync();
      }
      return { success, orderId, token: requestToken };
    },
    {
      manual: true,
      onSuccess(result) {
        if (
          unmounted.current ||
          currentContext.current.id !== result.orderId ||
          useSession.getState().token !== result.token
        ) {
          return;
        }
        notify(result.success ? '会员已开通，所有模板已解锁' : '已模拟支付失败，订单仍可重试');
      },
      onError(error) {
        notify(error.message);
      },
    },
  );
  const pay = useLockFn(async (success: boolean) => {
    await payment.runAsync(success).catch(() => {});
  });
  if (loading && !order) return <Loading />;
  if (!order) return <ErrorBox error={error} />;
  const seconds = Math.max(0, Math.floor(remainingTime / 1000));
  return (
    <main className="checkout-page">
      <Link className="back-link" to="/pricing">
        ← 返回会员权益
      </Link>
      <div className="checkout-card">
        <div className="checkout-icon">
          {order.status === 'PAID' ? <CheckCircle2 size={34} /> : <ShieldCheck size={34} />}
        </div>
        <span className="eyebrow">
          {order.status === 'PAID' ? 'READY FOR YOUR NEXT CHAPTER' : 'SECURE CHECKOUT'}
        </span>
        <h1>{checkoutTitle(order.status, seconds)}</h1>
        <p>
          {order.status === 'PAID'
            ? '下一份精彩，交给你来书写。'
            : '当前为支付沙箱，不会产生真实扣款'}
        </p>
        <div className="checkout-amount">¥{order.amount / 100}</div>
        <dl>
          <div>
            <dt>订单编号</dt>
            <dd>{order.orderNo}</dd>
          </div>
          <div>
            <dt>会员套餐</dt>
            <dd>
              {order.planName} · {order.durationDays} 天
            </dd>
          </div>
          <div>
            <dt>订单状态</dt>
            <dd>{statusText(order.status)}</dd>
          </div>
          {order.status === 'CREATED' && seconds > 0 && (
            <div>
              <dt>剩余支付时间</dt>
              <dd className="countdown">
                {String(Math.floor(seconds / 60)).padStart(2, '0')}:
                {String(seconds % 60).padStart(2, '0')}
              </dd>
            </div>
          )}
        </dl>
        <CheckoutActions
          status={order.status}
          remainingSeconds={seconds}
          mockEnabled={Boolean(config.data?.mockEnabled)}
          busy={payment.loading}
          onPay={(success) => void pay(success)}
        />
        <span className="checkout-security">
          <ShieldCheck size={14} />
          订单与会员权益由服务器验证
        </span>
      </div>
    </main>
  );
}
