import { useLockFn, useRequest } from 'ahooks';
import { CheckCircle2, Crown, Download, LayoutTemplate, ShieldCheck } from 'lucide-react';
import { useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { ErrorBox } from '../../../components/ui/ErrorBox';
import { Loading } from '../../../components/ui/Loading';
import { api } from '../../../lib/http';
import { notify } from '../../../stores/noticeStore';
import { useSession } from '../../../stores/sessionStore';
import type { Order, Plan } from '../../../types';
import { PlanCard } from '../components/PlanCard';
import { PricingFaq } from '../components/PricingFaq';

export function PricingPage() {
  const user = useSession((state) => state.user);
  const navigate = useNavigate();
  const requestKeys = useRef<Record<number, string>>({});
  const {
    data: plans,
    error,
    loading,
  } = useRequest(() => api<Plan[]>('GET', '/plans'), { cacheKey: 'plans', retryCount: 1 });
  const purchase = useRequest(
    async (plan: Plan) => {
      requestKeys.current[plan.id] ||= crypto.randomUUID();
      return api<Order>('POST', '/orders', {
        planId: plan.id,
        requestKey: requestKeys.current[plan.id],
      });
    },
    {
      manual: true,
      onSuccess(order) {
        navigate(`/checkout/${order.id}`);
      },
      onError(error) {
        notify(error.message);
      },
    },
  );
  const buy = useLockFn(async (plan: Plan) => {
    if (!user) {
      navigate('/login?redirect=/pricing');
      return;
    }
    await purchase.runAsync(plan).catch(() => {});
  });
  return (
    <main className="pricing-page">
      <div className="center-heading">
        <span className="pill-label">
          <Crown size={15} /> 简历工坊会员
        </span>
        <h1>
          为更好的下一步，<span>多一份从容。</span>
        </h1>
        <p>好内容值得好呈现。解锁所有模板，自由打磨每一个版本。</p>
        {user?.member && (
          <div className="member-status">
            <CheckCircle2 size={16} />
            会员有效期至 {user.memberExpireAt?.slice(0, 10)}
          </div>
        )}
      </div>
      {loading && !plans && <Loading />}
      {error && <ErrorBox error={error} />}
      <div className="price-grid">
        {plans?.map((plan) => (
          <PlanCard
            key={plan.id}
            plan={plan}
            isMember={Boolean(user?.member)}
            busy={purchase.loading && purchase.params[0]?.id === plan.id}
            disabled={purchase.loading}
            onBuy={() => void buy(plan)}
          />
        ))}
      </div>
      <div className="pricing-assurance">
        <span>
          <ShieldCheck size={18} />
          按期付费，无自动续费
        </span>
        <span>
          <Download size={18} />
          标准 A4 · 高清 PDF
        </span>
        <span>
          <LayoutTemplate size={18} />4 套原创模板
        </span>
      </div>
      <PricingFaq />
    </main>
  );
}
