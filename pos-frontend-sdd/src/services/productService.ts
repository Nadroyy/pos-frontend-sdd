// mockProducts.ts se conserva pero ya no se usa aquí.
// Todo el acceso a productos va al backend real en AWS.
import { API_BASE_URL } from '../config/api';
import type { Product } from '../types/product';

// Mapea el shape del backend { productId, name, ... } al tipo Product del frontend { id, ... }
function mapProduct(raw: Record<string, unknown>): Product {
  return {
    id:       String(raw.productId ?? raw.id ?? ''),
    name:     String(raw.name      ?? ''),
    category: String(raw.category  ?? ''),
    barcode:  String(raw.barcode   ?? ''),
    price:    Number(raw.price     ?? 0),
    taxRate:  Number(raw.taxRate   ?? 0),
    stock:    Number(raw.stock     ?? 0),
  };
}

/**
 * Busca productos en el backend.
 * Sin term → GET /productos          (lista completa)
 * Con term → GET /productos?q=<term> (filtrado por nombre/barcode/categoría)
 * El parámetro category se ignora porque el backend filtra por q.
 */
export async function searchProducts(term: string, _category: string): Promise<Product[]> {
  const url = term.trim()
    ? `${API_BASE_URL}/productos?q=${encodeURIComponent(term.trim())}`
    : `${API_BASE_URL}/productos`;

  const res = await fetch(url);
  if (!res.ok) throw new Error(`GET /productos falló: ${res.status}`);

  const data = await res.json() as { products: Record<string, unknown>[] };
  return data.products.map(mapProduct);
}

/**
 * Busca un producto por código de barras exacto.
 * El backend filtra por q=<barcode>; se elige el que coincide exactamente.
 */
export async function findProductByBarcode(barcode: string): Promise<Product | undefined> {
  const results = await searchProducts(barcode.trim(), 'Todos');
  return results.find(p => p.barcode === barcode.trim());
}
