import { Crown } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Button } from '../../../components/ui/Button';
import { type Order } from '../../../types/index';
import { statusText } from '../../membership/utils/orderStatus';

export function OrderList({
  orders,
  mockEnabled,
  onRefund,
}: {
  orders?: Order[];
  mockEnabled: boolean;
  onRefund: (order: Order) => void;
}) {
  return (
    <div className="order-list">
      {orders?.length === 0 && (
        <div className="empty-state">
          <Crown size={32} />
          <h3>还没有会员订单</h3>
          <p>想让简历拥有更多可能？</p>
          <Link className="button" to="/pricing">
            了解会员权益
          </Link>
        </div>
      )}
      {orders?.map((o) => (
        <div className="order-row" key={o.id}>
          <span className="order-symbol">
            <Crown size={22} />
          </span>
          <div>
            <b>{o.planName}</b>
            <small>{o.orderNo}</small>
            <small>{new Date(o.createdAt).toLocaleString('zh-CN')}</small>
          </div>
          <strong>¥{o.amount / 100}</strong>
          <span className={'order-status status-' + o.status}>{statusText(o.status)}</span>
          {o.status === 'CREATED' ? (
            <Link className="button small" to={'/checkout/' + o.id}>
              查看订单
            </Link>
          ) : o.status === 'PAID' && mockEnabled ? (
            <Button className="small" onClick={() => onRefund(o)}>
              退款演示
            </Button>
          ) : null}
        </div>
      ))}
    </div>
  );
}
