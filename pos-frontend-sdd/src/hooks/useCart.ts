import { useMemo, useState } from 'react';
import type { Product } from '../types/product';
import type { CartItem, Discount } from '../types/cart';
import { calculateSummary } from '../utils/totals';
export function useCart() {
 const [items,setItems]=useState<CartItem[]>([]);
 const [discount,setDiscount]=useState<Discount>({type:'none',value:0});
 const summary=useMemo(()=>calculateSummary(items,discount),[items,discount]);
 function addProduct(product: Product){ setItems(cur=>{ const ex=cur.find(i=>i.product.id===product.id); return ex ? cur.map(i=>i.product.id===product.id?{...i,quantity:i.quantity+1}:i) : [...cur,{product,quantity:1}]; });}
 function updateQuantity(productId:string, quantity:number){ if(quantity<=0){ setItems(cur=>cur.filter(i=>i.product.id!==productId)); return;} setItems(cur=>cur.map(i=>i.product.id===productId?{...i,quantity}:i)); }
 function removeProduct(productId:string){ setItems(cur=>cur.filter(i=>i.product.id!==productId)); }
 function clearCart(){ setItems([]); setDiscount({type:'none',value:0}); }
 return {items,discount,summary,addProduct,updateQuantity,removeProduct,setDiscount,clearCart};
}
