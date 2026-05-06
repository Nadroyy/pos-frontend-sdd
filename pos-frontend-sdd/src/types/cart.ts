import type { Product } from './product';
export type DiscountType = 'none' | 'percentage' | 'fixed';
export type Discount = { type: DiscountType; value: number; };
export type CartItem = { product: Product; quantity: number; };
export type CartSummary = { subtotal: number; discountTotal: number; taxTotal: number; grandTotal: number; };
