import { useEffect, useRef, useState } from 'react';
import type { Product } from '../../types/product';
import { formatMoney } from '../../utils/money';
import { searchProducts, findProductByBarcode } from '../../services/productService';

type Mode = 'idle' | 'search' | 'price' | 'barcode';

type Props = {
  onAdd: (product: Product) => 'ok' | 'no_stock' | 'not_found';
  inputRef?: React.RefObject<HTMLInputElement | null>;
  value: string;
  onChange: (v: string) => void;
};

function isPureBarcode(text: string): boolean {
  return /^\d+$/.test(text.trim());
}

function isPriceQuery(text: string): boolean {
  return text.trim().toLowerCase().startsWith('pv ') || text.trim().toLowerCase() === 'pv';
}

export function PosInput({ onAdd, inputRef, value, onChange }: Props) {
  const localRef = useRef<HTMLInputElement>(null);
  const ref = inputRef ?? localRef;

  const [feedback, setFeedback] = useState<{ type: 'ok' | 'error'; text: string } | null>(null);

  useEffect(() => { ref.current?.focus(); }, []);

  function showFeedback(type: 'ok' | 'error', text: string, ms = 2000) {
    setFeedback({ type, text });
    setTimeout(() => setFeedback(null), ms);
  }

  function detectMode(text: string): Mode {
    const t = text.trim();
    if (!t) return 'idle';
    if (isPriceQuery(t)) return 'price';
    if (isPureBarcode(t)) return 'barcode';
    return 'search';
  }

  const mode = detectMode(value);

  const searchResults: Product[] = mode === 'search'
    ? searchProducts(value.trim(), 'Todos').slice(0, 6)
    : [];

  const priceQuery = isPriceQuery(value) ? value.trim().slice(3).trim() : '';
  const priceResults: Product[] = mode === 'price' && priceQuery
    ? searchProducts(priceQuery, 'Todos')
    : [];

  function addProduct(product: Product) {
    const result = onAdd(product);
    if (result === 'ok') {
      showFeedback('ok', `+ ${product.name}`);
      onChange('');
      ref.current?.focus();
    } else if (result === 'no_stock') {
      showFeedback('error', 'Sin existencias.');
    } else {
      showFeedback('error', 'No encontrado.');
    }
  }

  function handleEnter() {
    const t = value.trim();
    if (!t) return;

    if (mode === 'barcode') {
      const product = findProductByBarcode(t);
      if (!product) { showFeedback('error', 'No encontrado.'); return; }
      addProduct(product);
      return;
    }

    if (mode === 'search') {
      if (searchResults.length === 1) addProduct(searchResults[0]);
      return;
    }

    if (mode === 'price') {
      if (priceResults.length === 0) showFeedback('error', 'No encontrado.');
      return;
    }
  }

  function handleKey(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === 'Enter') { e.preventDefault(); handleEnter(); }
    if (e.key === 'Escape') { onChange(''); setFeedback(null); }
  }

  const showResults = value.trim().length > 0 && (mode === 'search' || mode === 'price');

  return (
    <div className='search-wrap'>
      <div className='search-row'>
        <span className='search-prompt'>&gt;</span>
        <input
          ref={ref}
          value={value}
          onChange={e => { onChange(e.target.value); setFeedback(null); }}
          onKeyDown={handleKey}
          placeholder='codigo, nombre o pv producto'
          autoComplete='off'
          spellCheck={false}
          className='search-input'
        />
      </div>

      {feedback && (
        <div className={feedback.type === 'ok' ? 'fb-ok' : 'fb-err'}>{feedback.text}</div>
      )}

      {showResults && mode === 'search' && (
        <div className='results-list'>
          {searchResults.length === 0
            ? <div className='result-none'>Sin resultados</div>
            : searchResults.map(p => (
              <div key={p.id} className='result-row'>
                <span className='r-name'>{p.name}</span>
                <span className='r-meta'>{p.barcode} · exist:{p.stock}</span>
                <span className='r-price'>{formatMoney(p.price)}</span>
                <button
                  className='r-btn'
                  onClick={() => addProduct(p)}
                  disabled={p.stock === 0}
                  tabIndex={0}
                >
                  {p.stock === 0 ? 'sin stock' : 'agregar'}
                </button>
              </div>
            ))
          }
          {searchResults.length === 1 && (
            <div className='result-hint'>Enter para agregar</div>
          )}
        </div>
      )}

      {showResults && mode === 'price' && (
        <div className='results-list'>
          {priceResults.length === 0
            ? <div className='result-none'>No encontrado: {priceQuery}</div>
            : priceResults.map(p => (
              <div key={p.id} className='result-row'>
                <span className='r-name'>{p.name}</span>
                <span className='r-meta'>{p.category} · {p.barcode} · exist:{p.stock}</span>
                <span className='r-price-big'>{formatMoney(p.price)}</span>
              </div>
            ))
          }
        </div>
      )}
    </div>
  );
}
