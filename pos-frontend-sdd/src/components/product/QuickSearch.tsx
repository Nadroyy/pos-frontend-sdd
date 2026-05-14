import { useRef, useState } from 'react';
import type { Product } from '../../types/product';
import { formatMoney } from '../../utils/money';

type Props = {
  products: Product[];
  searchTerm: string;
  onSearchChange: (v: string) => void;
  onAdd: (p: Product) => void;
  searchInputRef?: React.RefObject<HTMLInputElement>;
};

export function QuickSearch({ products, searchTerm, onSearchChange, onAdd, searchInputRef }: Props) {
  const localRef = useRef<HTMLInputElement>(null);
  const ref = searchInputRef ?? localRef;
  const [addedId, setAddedId] = useState<string | null>(null);

  function handleAdd(p: Product) {
    onAdd(p);
    setAddedId(p.id);
    setTimeout(() => { setAddedId(null); }, 1200);
  }

  function handleKey(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === 'Enter' && products.length === 1) {
      e.preventDefault();
      handleAdd(products[0]);
    }
    if (e.key === 'Escape') {
      onSearchChange('');
    }
  }

  // Solo mostrar resultados cuando hay texto
  const showResults = searchTerm.trim().length > 0;

  return (
    <div className='quick-search-wrap'>
      <div className='quick-search-input-row'>
        <input
          ref={ref}
          value={searchTerm}
          onChange={e => onSearchChange(e.target.value)}
          onKeyDown={handleKey}
          placeholder='Buscar producto por nombre, categoría o código…'
          autoComplete='off'
          className='quick-search-input'
        />
        {searchTerm && (
          <button className='btn-ghost' onClick={() => onSearchChange('')} title='Limpiar (Esc)'>✕</button>
        )}
      </div>

      {showResults && (
        <div className='quick-results'>
          {products.length === 0 ? (
            <p className='muted qs-empty'>Sin resultados para «{searchTerm}»</p>
          ) : (
            <>
              {products.length === 1 && (
                <p className='hint-enter'>↵ Enter para agregar «{products[0].name}»</p>
              )}
              <ul className='qs-list'>
                {products.slice(0, 8).map(p => (
                  <li key={p.id} className='qs-item'>
                    <div className='qs-info'>
                      <span className='qs-name'>{p.name}</span>
                      <span className='qs-meta'>{p.category} · {p.barcode} · Exist: {p.stock}</span>
                    </div>
                    <div className='qs-right'>
                      <span className='qs-price'>{formatMoney(p.price)}</span>
                      <button
                        className={addedId === p.id ? 'btn-added btn-sm' : 'btn-sm'}
                        onClick={() => handleAdd(p)}
                        disabled={p.stock === 0}
                      >
                        {addedId === p.id ? '✓' : p.stock === 0 ? 'Sin stock' : 'Agregar'}
                      </button>
                    </div>
                  </li>
                ))}
                {products.length > 8 && (
                  <p className='muted qs-more'>+{products.length - 8} más — refina la búsqueda</p>
                )}
              </ul>
            </>
          )}
        </div>
      )}
    </div>
  );
}
