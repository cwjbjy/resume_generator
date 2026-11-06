import { ArrowRight, Check, Crown, FileText } from 'lucide-react';
import { Button } from '../../../components/ui/Button';
import type { Plan } from '../../../types';

interface PlanCardProps {
  plan: Plan;
  isMember: boolean;
  busy: boolean;
  disabled: boolean;
  onBuy: () => void;
}
export function PlanCard({ plan, isMember, busy, disabled, onBuy }: PlanCardProps) {
  const annual = plan.id === 2;
  return (
    <article className={`price-card ${annual ? 'featured' : ''}`}>
      {annual && <span className="recommend">更受欢迎 · 长期求职准备</span>}
      <span className="plan-icon">{annual ? <Crown size={24} /> : <FileText size={24} />}</span>
      <h2>{plan.name}</h2>
      <p>{annual ? '给成长留出充足的时间' : '适合正在冲刺的你'}</p>
      <div className="price">
        <span>¥</span>
        <strong>{plan.price / 100}</strong>
        <span>/ {plan.durationDays === 30 ? '30 天' : '365 天'}</span>
      </div>
      <div className="plan-divider" />
      <ul className="check-list">
        {(JSON.parse(plan.features) as string[]).map((feature) => (
          <li key={feature}>
            <Check size={17} />
            {feature}
          </li>
        ))}
      </ul>
      <Button
        busy={busy}
        disabled={disabled}
        className={annual ? 'primary full' : 'full'}
        onClick={onBuy}
      >
        {isMember ? '续期会员' : '开通会员'} <ArrowRight size={16} />
      </Button>
    </article>
  );
}
