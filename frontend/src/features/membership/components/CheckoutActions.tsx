import { ArrowRight, Check } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Button } from '../../../components/ui/Button';

interface CheckoutActionsProps {
  status: string;
  remainingSeconds: number;
  mockEnabled: boolean;
  busy: boolean;
  onPay: (success: boolean) => void;
}
export function CheckoutActions({
  status,
  remainingSeconds,
  mockEnabled,
  busy,
  onPay,
}: CheckoutActionsProps) {
  const navigate = useNavigate();
  if (status !== 'CREATED' || remainingSeconds <= 0) {
    const paid = status === 'PAID';
    return (
      <Button className="primary full" onClick={() => navigate(paid ? '/templates' : '/pricing')}>
        {paid ? '挑选会员模板' : '返回会员页面'}
        <ArrowRight size={16} />
      </Button>
    );
  }
  if (!mockEnabled)
    return <div className="inline-error">当前环境未开通真实支付渠道，请联系站点管理员。</div>;
  return (
    <>
      <Button className="primary full" busy={busy} onClick={() => onPay(true)}>
        <Check size={17} />
        模拟支付成功
      </Button>
      <Button className="text-button full" disabled={busy} onClick={() => onPay(false)}>
        模拟支付失败
      </Button>
    </>
  );
}
