import { mockProducts } from '../data/mockProducts';
import type { Product } from '../types/product';

export function searchProducts(term: string, category: string): Product[] {
  const clean = term.trim().toLowerCase();
  if (!clean && category === 'Todos') return mockProducts;
  return mockProducts.filter(p => {
    const matchesCat = category === 'Todos' || p.category === category;
    if (!clean) return matchesCat;
    const matchesTerm =
      p.name.toLowerCase().includes(clean) ||
      p.category.toLowerCase().includes(clean) ||
      p.barcode.includes(clean);
    return matchesCat && matchesTerm;
  });
}

export function findProductByBarcode(barcode: string): Product | undefined {
  return mockProducts.find(p => p.barcode === barcode.trim());
}
