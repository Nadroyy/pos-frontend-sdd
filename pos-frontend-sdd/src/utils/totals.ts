import type { CartItem, CartSummary, Discount } from '../types/cart';
import { roundMoney } from './money';
export function calculateSummary(items: CartItem[], discount: Discount): CartSummary {
 const subtotal = roundMoney(items.reduce((s,i)=>s+i.product.price*i.quantity,0));
 const rawDiscount = discount.type === 'percentage' ? subtotal * (discount.value/100) : discount.type === 'fixed' ? discount.value : 0;
 const discountTotal = roundMoney(Math.min(Math.max(rawDiscount,0),subtotal));
 const taxableBase = Math.max(subtotal-discountTotal,0);
 const taxTotal = roundMoney(items.reduce((s,i)=>{ const line=i.product.price*i.quantity; const base=subtotal>0 ? line/subtotal*taxableBase : 0; return s+base*i.product.taxRate; },0));
 return { subtotal, discountTotal, taxTotal, grandTotal: roundMoney(taxableBase+taxTotal) };
}
