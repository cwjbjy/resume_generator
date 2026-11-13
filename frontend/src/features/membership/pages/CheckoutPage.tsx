import { useCountDown, useLockFn, useRequest, useUnmountedRef } from 'ahooks';
import { CheckCircle2, ShieldCheck } from 'lucide-react';
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
  const unmounted = useUnmountedRef();
  const { data, error, loading, refreshAsync, cancel } = useRequest(
    () => membershipApi.getOrder(id),
    {
      refreshDeps: [id],
      retryCount: 1,
      pollingInterval: 3000,
      pollingWhenHidden: false,
      onSuccess(order) {
        if (order.status !== 'CREATED') cancel();
      },
    },
  );
  const order = data?.id === Number(id) ? data : undefined;
  const [remainingTime] = useCountDown({ targetDate: order?.expiredAt });
  const config = useRuntimeConfig();
  const payment = useRequest(
    async (success: boolean) => {
      const token = useSession.getState().token;
      await membershipApi.mockPayment(Number(id), success);
      if (unmounted.current || useSession.getState().token !== token) return success;
      await refreshAsync();
      if (success) {
        await refreshUser();
      }
      return success;
    },
    {
      manual: true,
      onSuccess(success) {
        notify(success ? '会员已开通，所有模板已解锁' : '已模拟支付失败，订单仍可重试');
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
            <dd>{order.planId === 1 ? '月度会员 · 30 天' : '年度会员 · 365 天'}</dd>
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
