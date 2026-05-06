import type { CartItem, CartSummary } from './cart';
import type { Payment } from './payment';
export type Receipt = { receiptNumber: string; createdAt: string; items: CartItem[]; summary: CartSummary; payments: Payment[]; change: number; };
