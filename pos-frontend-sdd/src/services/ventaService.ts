import { API_BASE_URL } from '../config/api';
import type { CartItem } from '../types/cart';

export type VentaResponse = {
  saleId:    string;
  status:    string;
  timestamp: string;
};

/**
 * Registra una venta en el backend.
 * POST /ventas → { saleId, status, timestamp }
 */
export async function registrarVenta(
  items: CartItem[],
  total: number,
): Promise<VentaResponse> {
  const body = {
    items: items.map(i => ({
      productId: i.product.id,
      name:      i.product.name,
      quantity:  i.quantity,
      price:     i.product.price,
    })),
    total,
    paymentMethod: 'CASH',
  };

  const res = await fetch(`${API_BASE_URL}/ventas`, {
    method:  'POST',
    headers: { 'Content-Type': 'application/json' },
    body:    JSON.stringify(body),
  });

  if (!res.ok) throw new Error(`POST /ventas falló: ${res.status}`);
  return res.json() as Promise<VentaResponse>;
}
