import { api } from '../../../lib/http';
import type { Order, Plan } from '../../../types';

export const membershipApi = {
  listPlans() {
    return api<Plan[]>('GET', '/plans');
  },
  listOrders() {
    return api<Order[]>('GET', '/orders');
  },
  getOrder(id: string | number | undefined) {
    return api<Order>('GET', `/orders/${id}`);
  },
  createOrder(request: { planId: number; requestKey: string }) {
    return api<Order>('POST', '/orders', request);
  },
  mockPayment(orderId: number, success: boolean) {
    return api('POST', '/pay/mock', { orderId, success });
  },
  refundOrder(id: number) {
    return api('POST', `/orders/${id}/refund`);
  },
};
