export function checkoutTitle(status: string, remainingSeconds: number) {
  if (status === 'PAID') return '会员开通成功';
  if (status === 'REFUNDED') return '订单已退款';
  if (status === 'EXPIRED' || remainingSeconds <= 0) return '订单已过期';
  return '确认你的会员订单';
}
