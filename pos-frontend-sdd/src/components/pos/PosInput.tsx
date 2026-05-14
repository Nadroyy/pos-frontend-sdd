import { useEffect, useRef, useState } from 'react';
import type { Product } from '../../types/product';
import { formatMoney } from '../../utils/money';
import { searchProducts, findProductByBarcode } from '../../services/productService';

// ── Tipos de resultado ────────────────────────────────────────────────────
type Mode = 'idle' | 'search' | 'price' | 'barcode';

interface PriceResult {
  product: Product;
}

type Props = {
  onAdd: (product: Product) => 'ok' | 'no_stock' | 'not_found';
  inputRef?: React.RefObject<HTMLInputElement>;
};

// ── Helpers ───────────────────────────────────────────────────────────────
function isPureBarcode(text: string): boolean {
  // Código de barras: solo dígitos, sin espacios
  return /^\d+$/.test(text.trim());
}

function isPriceQuery(text: string): boolean {
  return text.trim().toLowerCase().startsWith('pv ') || text.trim().toLowerCase() === 'pv';
}

// ── Componente ────────────────────────────────────────────────────────────
export function PosInput({ onAdd, inputRef }: Props) {
  const localRef = useRef<HTMLInputElement>(null);
  const ref = inputRef ?? localRef;

  const [value, setValue]         = useState('');
  const [feedback, setFeedback]   = useState<{ type: 'ok' | 'error' | 'info'; text: string } | null>(null);
  const [addedId, setAddedId]     = useState<string | null>(null);

  // Autofoco al montar
  useEffect(() => { ref.current?.focus(); }, []);

  // Limpiar feedback después de un tiempo
  function showFeedback(type: 'ok' | 'error' | 'info', text: string, ms = 2200) {
    setFeedback({ type, text });
    setTimeout(() => setFeedback(null), ms);
  }

  // ── Detectar modo según el texto ─────────────────────────────────────
  function detectMode(text: string): Mode {
    const t = text.trim();
    if (!t) return 'idle';
    if (isPriceQuery(t)) return 'price';
    if (isPureBarcode(t)) return 'barcode';
    return 'search';
  }

  const mode = detectMode(value);

  // ── Resultados de búsqueda (solo en modo search) ──────────────────────
  const searchResults: Product[] = mode === 'search'
    ? searchProducts(value.trim(), 'Todos').slice(0, 8)
    : [];

  // ── Resultados de consulta de precio (modo price) ─────────────────────
  const priceQuery = isPriceQuery(value) ? value.trim().slice(3).trim() : '';
  const priceResults: PriceResult[] = mode === 'price' && priceQuery
    ? searchProducts(priceQuery, 'Todos').map(p => ({ product: p }))
    : [];

  // ── Agregar producto ──────────────────────────────────────────────────
  function addProduct(product: Product) {
    const result = onAdd(product);
    if (result === 'ok') {
      setAddedId(product.id);
      setTimeout(() => setAddedId(null), 1200);
      showFeedback('ok', `Producto agregado: ${product.name}`);
      setValue('');
      ref.current?.focus();
    } else if (result === 'no_stock') {
      showFeedback('error', 'Existencias insuficientes.');
    } else {
      showFeedback('error', 'Producto no encontrado.');
    }
  }

  // ── Enter ─────────────────────────────────────────────────────────────
  function handleEnter() {
    const t = value.trim();
    if (!t) return;

    if (mode === 'barcode') {
      // Código exacto → buscar y agregar
      const product = findProductByBarcode(t);
      if (!product) {
        showFeedback('error', 'Producto no encontrado.');
        return;
      }
      addProduct(product);
      return;
    }

    if (mode === 'search') {
      // Si hay exactamente 1 resultado → agregar
      if (searchResults.length === 1) {
        addProduct(searchResults[0]);
      }
      // Si hay 0 o varios → no hacer nada (el usuario ve la lista)
      return;
    }

    if (mode === 'price') {
      // Solo consulta, no agrega
      if (priceResults.length === 0) {
        showFeedback('error', 'Producto no encontrado.');
      }
      return;
    }
  }

  function handleKey(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === 'Enter') { e.preventDefault(); handleEnter(); }
    if (e.key === 'Escape') { setValue(''); setFeedback(null); }
  }

  const showResults = value.trim().length > 0 && (mode === 'search' || mode === 'price');

  return (
    <div className='pos-input-wrap'>
      {/* ── Input principal ── */}
      <div className='pos-input-row'>
        <input
          ref={ref}
          value={value}
          onChange={e => { setValue(e.target.value); setFeedback(null); }}
          onKeyDown={handleKey}
          placeholder='Ej: 200001, leche, pv pan'
          autoComplete='off'
          className='pos-main-input'
          spellCheck={false}
        />
        {value && (
          <button
            className='btn-ghost pos-clear-btn'
            onClick={() => { setValue(''); setFeedback(null); ref.current?.focus(); }}
            title='Limpiar (Esc)'
          >✕</button>
        )}
      </div>

      {/* ── Indicador de modo ── */}
      {value.trim() && (
        <div className='pos-mode-badge'>
          {mode === 'barcode' && <span className='badge badge-blue'>Código de barras — Enter para agregar</span>}
          {mode === 'search'  && searchResults.length === 1 && (
            <span className='badge badge-blue'>↵ Enter para agregar «{searchResults[0].name}»</span>
          )}
          {mode === 'price'   && <span className='badge badge-gray'>Consulta de precio — solo visualización</span>}
        </div>
      )}

      {/* ── Feedback ── */}
      {feedback && (
        <p className={feedback.type === 'ok' ? 'msg-success' : feedback.type === 'error' ? 'error' : 'muted'}>
          {feedback.text}
        </p>
      )}

      {/* ── Resultados ── */}
      {showResults && (
        <div className='pos-results'>

          {/* Modo búsqueda */}
          {mode === 'search' && (
            searchResults.length === 0
              ? <p className='muted qs-empty'>Sin resultados para «{value}»</p>
              : <ul className='qs-list'>
                  {searchResults.map(p => (
                    <li key={p.id} className='qs-item'>
                      <div className='qs-info'>
                        <span className='qs-name'>{p.name}</span>
                        <span className='qs-meta'>{p.category} · {p.barcode} · Exist: {p.stock}</span>
                      </div>
                      <div className='qs-right'>
                        <span className='qs-price'>{formatMoney(p.price)}</span>
                        <button
                          className={`btn-sm ${addedId === p.id ? 'btn-added' : ''}`}
                          onClick={() => addProduct(p)}
                          disabled={p.stock === 0}
                        >
                          {addedId === p.id ? '✓' : p.stock === 0 ? 'Sin stock' : 'Agregar'}
                        </button>
                      </div>
                    </li>
                  ))}
                  {searchProducts(value.trim(), 'Todos').length > 8 && (
                    <p className='muted qs-more'>
                      +{searchProducts(value.trim(), 'Todos').length - 8} más — refina la búsqueda
                    </p>
                  )}
                </ul>
          )}

          {/* Modo consulta de precio */}
          {mode === 'price' && (
            priceResults.length === 0 && priceQuery
              ? <p className='muted qs-empty'>Producto no encontrado para «{priceQuery}»</p>
              : <ul className='qs-list'>
                  {priceResults.map(({ product: p }) => (
                    <li key={p.id} className='qs-item qs-item-price'>
                      <div className='qs-info'>
                        <span className='qs-name'>{p.name}</span>
                        <span className='qs-meta'>{p.category} · Código: {p.barcode} · Exist: {p.stock}</span>
                      </div>
                      <div className='qs-right'>
                        <span className='qs-price-big'>{formatMoney(p.price)}</span>
                      </div>
                    </li>
                  ))}
                </ul>
          )}

        </div>
      )}
    </div>
  );
}
