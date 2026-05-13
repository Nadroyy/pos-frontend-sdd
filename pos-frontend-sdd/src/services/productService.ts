import { mockProducts } from '../data/mockProducts';
import type { Product } from '../types/product';
export function searchProducts(term: string, category: string): Product[] {
  const clean = term.trim().toLowerCase();
  return mockProducts.filter(p => p.name.toLowerCase().includes(clean) && (category === 'Todos' || p.category === category));
}
export function findProductByBarcode(barcode: string): Product | undefined {
  return mockProducts.find(p => p.barcode === barcode.trim());
}
