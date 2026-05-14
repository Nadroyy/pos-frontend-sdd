import { useRef, useState } from 'react';
import type { Product } from '../../types/product';
import { formatMoney } from '../../utils/money';

type Props = {
  products: Product[];
  searchTerm: string;
  category: string;
  categories: string[];
  onSearchChange: (v: string) => void;
  onCategoryChange: (v: string) => void;
  onAdd: (p: Product) => void;
  searchInputRef?: React.RefObject<HTMLInputElement>;
};

export function ProductSearch({
  products, searchTerm, category, categories,
  onSearchChange, onCategoryChange, onAdd, searchInputRef,
}: Props) {
  const localRef = useRef<HTMLInputElement>(null);
  const ref = searchInputRef ?? localRef;
  const [addedId, setAddedId] = useState<string | null>(null);

  function handleAdd(p: Product) {
    onAdd(p);
    setAddedId(p.id);
    setTimeout(() => setAddedId(null), 1200);
  }

  function handleSearchKey(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === 'Enter' && products.length === 1) {
      e.preventDefault();
      handleAdd(products[0]);
    }
    if (e.key === 'Escape') { onSearchChange(''); }
  }

  return (
    <section className='panel'>
      <h2>Búsqueda de productos <kbd>Ctrl+K</kbd></h2>
      <div className='grid two'>
        <label>
          Buscar por nombre, categoría o código
          <input
            ref={ref}
            value={searchTerm}
            onChange={e => onSearchChange(e.target.value)}
            onKeyDown={handleSearchKey}
            placeholder='Leche, pan, 200001...'
            autoComplete='off'
          />
        </label>
        <label>
          Categoría
          <select value={category} onChange={e => onCategoryChange(e.target.value)}>
            {categories.map(c => <option key={c}>{c}</option>)}
          </select>
        </label>
      </div>
      {products.length === 1 && searchTerm && (
        <p className='hint-enter'>↵ Enter para agregar «{products[0].name}»</p>
      )}
      <div>
        {products.length === 0
          ? <p className='muted'>No se encontraron productos.</p>
          : products.map(p => (
            <article className='product-card' key={p.id}>
              <div>
                <strong>{p.name}</strong>
                <p>{p.category} — Código {p.barcode}</p>
                <p>Existencias: {p.stock}</p>
              </div>
              <div className='right'>
                <strong>{formatMoney(p.price)}</strong>
                <button
                  onClick={() => handleAdd(p)}
                  className={addedId === p.id ? 'btn-added' : ''}
                >
                  {addedId === p.id ? '✓ Agregado' : 'Agregar'}
                </button>
              </div>
            </article>
          ))
        }
      </div>
    </section>
  );
}
