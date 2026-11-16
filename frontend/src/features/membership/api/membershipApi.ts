import { api } from '../../../lib/http';
import type { Order, PageResult, Plan } from '../../../types';

export const membershipApi = {
  listPlans() {
    return api<Plan[]>('GET', '/plans');
  },
  listOrders(page = 1, size = 12) {
    return api<PageResult<Order>>('GET', `/orders?page=${page}&size=${size}`);
  },
  getOrder(id: string | number | undefined, token?: string) {
    return api<Order>('GET', `/orders/${id}`, undefined, token);
  },
  createOrder(request: { planId: number; requestKey: string }) {
    return api<Order>('POST', '/orders', request);
  },
  mockPayment(orderId: number, success: boolean, token?: string) {
    return api<Order>('POST', '/pay/mock', { orderId, success }, token);
  },
  refundOrder(id: number) {
    return api<Order>('POST', `/orders/${id}/refund`);
  },
};
