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

  const [receipt, setReceipt]         = useState<Receipt | null>(null);
  const [searchValue, setSearchValue] = useState('');

  const mainInputRef = useRef<HTMLInputElement>(null);
  const amountRef    = useRef<HTMLInputElement>(null);

  // ── Atajos globales ─────────────────────────────────────────────────────
  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      const tag     = (e.target as HTMLElement).tagName;
      const inInput = tag === 'INPUT' || tag === 'SELECT' || tag === 'TEXTAREA';

      // F2 → buscador
      if (e.key === 'F2') {
        e.preventDefault();
        mainInputRef.current?.focus();
        mainInputRef.current?.select();
        return;
      }

      // F4 → input de monto en cobro
      if (e.key === 'F4') {
        e.preventDefault();
        amountRef.current?.focus();
        amountRef.current?.select();
        return;
      }

      // Escape: si hay búsqueda activa → limpiarla; si no → vaciar carrito con confirm
      if (e.key === 'Escape') {
        if (searchValue.trim()) {
          setSearchValue('');
          mainInputRef.current?.focus();
        } else if (!inInput && cart.items.length > 0) {
          if (window.confirm('¿Vaciar el carrito?')) {
            cart.clearCart();
          }
          mainInputRef.current?.focus();
        }
      }
    }
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [searchValue, cart]);

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
    return (
      <div className='pos-screen'>
        <ReceiptView receipt={receipt} onNewSale={newSale} />
      </div>
    );
  }

  return (
    <div className='pos-screen'>

      {/* Título + estado */}
      <div className='pos-title-row'>
        <span className='pos-title'>POS Supermercado</span>
        <span className={online ? 'pos-status-on' : 'pos-status-off'}>
          {online ? 'en linea' : 'sin conexion'}
        </span>
      </div>

      {/* Atajos — fijos arriba, antes del buscador */}
      <div className='pos-help'>
        F2 Buscar &nbsp;&nbsp; Enter Agregar &nbsp;&nbsp; pv Precio &nbsp;&nbsp; F4 Cobro &nbsp;&nbsp; Esc Limpiar
      </div>

      {/* Buscador principal */}
      <PosInput
        onAdd={handleAdd}
        inputRef={mainInputRef}
        value={searchValue}
        onChange={setSearchValue}
      />

      {/* Carrito + totales */}
      <CartPanel
        items={cart.items}
        summary={cart.summary}
        discount={cart.discount}
        onQuantity={cart.updateQuantity}
        onRemove={cart.removeProduct}
        onDiscount={cart.setDiscount}
        onClear={cart.clearCart}
      />

      {/* Cobro */}
      <CheckoutPanel
        items={cart.items}
        summary={cart.summary}
        onComplete={setReceipt}
        amountRef={amountRef}
      />

    </div>
  );
}
