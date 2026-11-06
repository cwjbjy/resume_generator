import { useQuery, useQueryClient } from '@tanstack/react-query';
import { CheckCircle2, ShieldCheck } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { refreshUser } from '../../../api/userApi';
import { ErrorBox } from '../../../components/ui/ErrorBox';
import { Loading } from '../../../components/ui/Loading';
import { useRuntimeConfig } from '../../../hooks/useRuntimeConfig';
import { api } from '../../../lib/http';
import { notify } from '../../../stores/noticeStore';
import { type Order } from '../../../types/index';
import { CheckoutActions } from '../components/CheckoutActions';
import { checkoutTitle } from '../utils/checkoutTitle';
import { statusText } from '../utils/orderStatus';
export function CheckoutPage() {
  const { id } = useParams();
  const query = useQueryClient();
  const [busy, setBusy] = useState(false);
  const [now, setNow] = useState(() => Date.now());
  const {
    data: order,
    error,
    isLoading,
  } = useQuery({
    queryKey: ['order', id],
    queryFn: () => api<Order>('GET', '/orders/' + id),
    refetchInterval: (q) => (q.state.data?.status === 'CREATED' ? 3000 : false),
  });
  const config = useRuntimeConfig();
  useEffect(() => {
    const t = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(t);
  }, []);
  async function pay(success: boolean) {
    setBusy(true);
    try {
      await api('POST', '/pay/mock', { orderId: Number(id), success });
      await query.invalidateQueries({ queryKey: ['order', id] });
      if (success) {
        await refreshUser();
        query.invalidateQueries({ queryKey: ['user'] });
        notify('会员已开通，所有模板已解锁');
      } else notify('已模拟支付失败，订单仍可重试');
    } catch (e) {
      notify((e as Error).message);
    } finally {
      setBusy(false);
    }
  }
  if (isLoading) return <Loading />;
  if (!order) return <ErrorBox error={error} />;
  const seconds = Math.max(0, Math.floor((new Date(order.expiredAt).getTime() - now) / 1000));
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
          busy={busy}
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
