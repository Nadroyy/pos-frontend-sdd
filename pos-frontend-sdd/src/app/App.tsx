import { useEffect, useRef, useState } from 'react';
import { PosInput } from '../components/pos/PosInput';
import { CartPanel } from '../components/cart/CartPanel';
import { CheckoutPanel } from '../components/checkout/CheckoutPanel';
import { ReceiptView } from '../components/receipt/ReceiptView';
import { useCart } from '../hooks/useCart';
import { useOnlineStatus } from '../hooks/useOnlineStatus';
import type { Product } from '../types/product';
import type { Receipt } from '../types/receipt';

export function App() {
  const online = useOnlineStatus();
  const cart   = useCart();

  const [receipt, setReceipt] = useState<Receipt | null>(null);

  const mainInputRef = useRef<HTMLInputElement>(null);
  const checkoutRef  = useRef<HTMLElement>(null);

  // ── Atajos globales ─────────────────────────────────────────────────────
  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if (e.key === 'F2' || (e.ctrlKey && e.key === 'k')) {
        e.preventDefault();
        mainInputRef.current?.focus();
        mainInputRef.current?.select();
        return;
      }
      if (e.key === 'F4') {
        e.preventDefault();
        checkoutRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
        const first = checkoutRef.current?.querySelector('input,select') as HTMLElement | null;
        first?.focus();
        return;
      }
      const tag = (e.target as HTMLElement).tagName;
      const inInput = tag === 'INPUT' || tag === 'SELECT' || tag === 'TEXTAREA';
      if (e.key === 'Escape' && !inInput) {
        mainInputRef.current?.focus();
      }
    }
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, []);

  function handleAdd(product: Product): 'ok' | 'no_stock' | 'not_found' {
    const inCart = cart.items.find(i => i.product.id === product.id)?.quantity ?? 0;
    if (product.stock <= inCart) return 'no_stock';
    cart.addProduct(product);
    return 'ok';
  }

  function newSale() {
    setReceipt(null);
    cart.clearCart();
    setTimeout(() => mainInputRef.current?.focus(), 80);
  }

  if (receipt) {
    return <main><ReceiptView receipt={receipt} onNewSale={newSale} /></main>;
  }

  return (
    <main>

      {/* ── Topbar ── */}
      <header className='topbar'>
        <div className='topbar-brand'>
          <h1>POS Supermercado</h1>
        </div>
        <div className='topbar-right'>
          <div className='shortcuts-bar'>
            <span><kbd>F2</kbd>/<kbd>Ctrl+K</kbd> buscar</span>
            <span><kbd>Enter</kbd> agregar</span>
            <span><kbd>pv</kbd> precio</span>
            <span><kbd>F4</kbd> cobro</span>
            <span><kbd>Esc</kbd> limpiar</span>
          </div>
          <span className={online ? 'status online' : 'status offline'}>
            {online ? 'En línea' : 'Sin conexión'}
          </span>
        </div>
      </header>

      {/* ── Input principal ── */}
      <section className='panel pos-input-panel'>
        <div className='pos-input-header'>
          <span className='pos-input-label'>Escanear código, buscar producto o consultar precio</span>
          <kbd>F2</kbd>
        </div>
        <PosInput onAdd={handleAdd} inputRef={mainInputRef} />
      </section>

      {/* ── Zona central: carrito + cobro ── */}
      <div className='pos-layout'>

        <div className='pos-left'>
          <CartPanel
            items={cart.items}
            summary={cart.summary}
            discount={cart.discount}
            onQuantity={cart.updateQuantity}
            onRemove={cart.removeProduct}
            onDiscount={cart.setDiscount}
            onClear={cart.clearCart}
          />
        </div>

        <div className='pos-right'>
          <CheckoutPanel
            items={cart.items}
            summary={cart.summary}
            onComplete={setReceipt}
            checkoutRef={checkoutRef}
          />
        </div>

      </div>
    </main>
  );
}
