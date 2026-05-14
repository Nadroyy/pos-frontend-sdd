import { useEffect, useRef, useState } from 'react';
import type { Product } from '../../types/product';
import { formatMoney } from '../../utils/money';

// ── Tipos de modo ────────────────────────────────────────────────────────────
type Mode = 'idle' | 'search' | 'price';

type PriceResult = { product: Product };

type Props = {
  products: Product[];          // resultados filtrados desde App
  searchTerm: string;
  onSearchChange: (v: string) => void;
  onAdd: (p: Product) => boolean; // devuelve true si se agregó, false si no hay stock
  inputRef?: React.RefObject<HTMLInputElement>;
};

// ── Helpers ──────────────────────────────────────────────────────────────────

/** Detecta si el texto es un código de barras numérico exacto */
function isExactBarcode(term: string, products: Product[]): Product | null {
  const clean = term.trim();
  if (!/^\d+$/.test(clean)) return null;
  return products.find(p => p.barcode === clean) ?? null;
}

/** Detecta el prefijo "pv " */
function isPriceQuery(term: string): string | null {
  const m = term.match(/^pv\s+(.+)/i);
  return m ? m[1].trim() : null;
}

// ── Componente ───────────────────────────────────────────────────────────────
export function MainInput({ products, searchTerm, onSearchChange, onAdd, inputRef }: Props) {
  const localRef = useRef<HTMLInputElement>(null);
  const ref = inputRef ?? localRef;

  const [feedback, setFeedback] = useState<{ type: 'ok' | 'err'; msg: string } | null>(null);
  const [addedId,  setAddedId]  = useState<string | null>(null);

  // Autofoco al montar
  useEffect(() => { ref.current?.focus(); }, []);

  // Limpiar feedback automáticamente
  useEffect(() => {
    if (!feedback) return;
    const t = setTimeout(() => setFeedback(null), 2000);
    return () => clearTimeout(t);
  }, [feedback]);

  // ── Determinar modo ──────────────────────────────────────────────────────
  const priceQuery = isPriceQuery(searchTerm);
  const mode: Mode = priceQuery !== null ? 'price'
    : searchTerm.trim().length > 0 ? 'search'
    : 'idle';

  // Resultados para consulta de precio
  const priceResults: PriceResult[] = priceQuery
    ? products
        .filter(p =>
          p.name.toLowerCase().includes(priceQuery.toLowerCase()) ||
          p.barcode.includes(priceQuery)
        )
        .map(p => ({ product: p }))
    : [];

  // ── Handlers ─────────────────────────────────────────────────────────────
  function handleAdd(p: Product) {
    const ok = onAdd(p);
    if (ok) {
      setAddedId(p.id);
      setTimeout(() => setAddedId(null), 1200);
      setFeedback({ type: 'ok', msg: `Producto agregado: ${p.name}` });
      onSearchChange('');
      ref.current?.focus();
    } else {
      setFeedback({ type: 'err', msg: `Existencias insuficientes: ${p.name}` });
    }
  }

  function handleEnter() {
    const term = searchTerm.trim();
    if (!term) return;

    // Modo precio → no agrega
    if (mode === 'price') return;

    // Código numérico exacto → agregar directo
    const exact = isExactBarcode(term, products);
    if (exact) {
      handleAdd(exact);
      return;
    }

    // Un solo resultado de búsqueda → agregar
    if (mode === 'search' && products.length === 1) {
      handleAdd(products[0]);
      return;
    }

    // Varios resultados → no hace nada, el usuario elige
  }

  function handleKey(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === 'Enter') { e.preventDefault(); handleEnter(); }
    if (e.key === 'Escape') { onSearchChange(''); setFeedback(null); ref.current?.focus(); }
  }

  // ── Render ────────────────────────────────────────────────────────────────
  const showResults = mode === 'search' || mode === 'price';

  return (
    <div className='main-input-wrap'>
      {/* Input principal */}
      <div className='main-input-row'>
        <input
          ref={ref}
          value={searchTerm}
          onChange={e => { onSearchChange(e.target.value); setFeedback(null); }}
          onKeyDown={handleKey}
          placeholder='Ej: 200001, leche, pv pan'
          autoComplete='off'
          className='main-input'
          spellCheck={false}
        />
        {searchTerm && (
          <button
            className='btn-ghost btn-clear'
            onClick={() => { onSearchChange(''); ref.current?.focus(); }}
            title='Limpiar (Esc)'
          >✕</button>
        )}
      </div>

      {/* Feedback de acción */}
      {feedback && (
        <p className={feedback.type === 'ok' ? 'msg-success' : 'error'}>
          {feedback.msg}
        </p>
      )}

      {/* Resultados */}
      {showResults && (
        <div className='main-results'>

          {/* ── Modo precio ── */}
          {mode === 'price' && (
            <>
              <div className='results-header'>
                Consulta de precio: <strong>{priceQuery}</strong>
              </div>
              {priceResults.length === 0 ? (
                <p className='muted res-empty'>Producto no encontrado.</p>
              ) : (
                <ul className='res-list'>
                  {priceResults.map(({ product: p }) => (
                    <li key={p.id} className='res-item res-price-item'>
                      <div className='res-info'>
                        <span className='res-name'>{p.name}</span>
                        <span className='res-meta'>
                          {p.category} · Código: {p.barcode} · Existencias: {p.stock}
                        </span>
                      </div>
                      <span className='res-price-big'>{formatMoney(p.price)}</span>
                    </li>
                  ))}
                </ul>
              )}
            </>
          )}

          {/* ── Modo búsqueda ── */}
          {mode === 'search' && (
            <>
              {products.length === 0 ? (
                <p className='muted res-empty'>Producto no encontrado.</p>
              ) : (
                <>
                  {products.length === 1 && (
                    <p className='hint-enter'>↵ Enter para agregar «{products[0].name}»</p>
                  )}
                  <ul className='res-list'>
                    {products.slice(0, 8).map(p => (
                      <li key={p.id} className='res-item'>
                        <div className='res-info'>
                          <span className='res-name'>{p.name}</span>
                          <span className='res-meta'>
                            {p.category} · {p.barcode} · Exist: {p.stock}
                          </span>
                        </div>
                        <div className='res-right'>
                          <span className='res-price'>{formatMoney(p.price)}</span>
                          <button
                            className={`btn-sm ${addedId === p.id ? 'btn-added' : ''}`}
                            onClick={() => handleAdd(p)}
                            disabled={p.stock === 0}
                          >
                            {addedId === p.id ? '✓' : p.stock === 0 ? 'Sin stock' : 'Agregar'}
                          </button>
                        </div>
                      </li>
                    ))}
                    {products.length > 8 && (
                      <p className='muted res-more'>
                        +{products.length - 8} más — refina la búsqueda
                      </p>
                    )}
                  </ul>
                </>
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
}
