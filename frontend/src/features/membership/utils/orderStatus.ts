export function statusText(status: string) {
  return (
    (
      { CREATED: '待支付', PAID: '已支付', EXPIRED: '已过期', REFUNDED: '已退款' } as Record<
        string,
        string
      >
    )[status] || status
  );
}
