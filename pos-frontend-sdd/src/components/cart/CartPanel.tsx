import type { CartItem, CartSummary, Discount } from '../../types/cart';
import { formatMoney } from '../../utils/money';

type Props = {
  items: CartItem[];
  summary: CartSummary;
  discount: Discount;
  onQuantity: (id: string, q: number) => void;
  onRemove: (id: string) => void;
  onDiscount: (d: Discount) => void;
  onClear: () => void;
};

export function CartPanel({ items, summary, discount, onQuantity, onRemove, onDiscount, onClear }: Props) {
  function handleClear() {
    if (items.length === 0) return;
    if (window.confirm('¿Vaciar el carrito? Se eliminarán todos los productos.')) {
      onClear();
    }
  }

  return (
    <section className='panel'>
      <div className='cart-header'>
        <h2>Carrito ({items.length})</h2>
        {items.length > 0 && (
          <button className='danger btn-sm' onClick={handleClear} title='Ctrl+Retroceso'>
            Vaciar <kbd>Ctrl+⌫</kbd>
          </button>
        )}
      </div>

      {items.length === 0
        ? <p className='muted'>El carrito está vacío.</p>
        : items.map(i => (
          <article className='cart-row' key={i.product.id}>
            <div>
              <strong>{i.product.name}</strong>
              <p>{formatMoney(i.product.price)} — IVA {(i.product.taxRate * 100).toFixed(0)}%</p>
            </div>
            <div className='qty'>
              <button onClick={() => onQuantity(i.product.id, i.quantity - 1)}>−</button>
              <span>{i.quantity}</span>
              <button onClick={() => onQuantity(i.product.id, i.quantity + 1)}>+</button>
            </div>
            <strong>{formatMoney(i.product.price * i.quantity)}</strong>
            <button className='danger' onClick={() => onRemove(i.product.id)}>✕</button>
          </article>
        ))
      }

      <div className='discount'>
        <h3>Descuento</h3>
        <select
          value={discount.type}
          onChange={e => onDiscount({ type: e.target.value as Discount['type'], value: discount.value })}
        >
          <option value='none'>Sin descuento</option>
          <option value='percentage'>Porcentaje (%)</option>
          <option value='fixed'>Monto fijo ($)</option>
        </select>
        <input
          type='number' min='0'
          value={discount.value}
          onChange={e => onDiscount({ ...discount, value: Number(e.target.value) })}
        />
        <button onClick={() => onDiscount({ type: 'none', value: 0 })}>Restablecer</button>
      </div>

      <div className='summary'>
        <p>Subtotal   <strong>{formatMoney(summary.subtotal)}</strong></p>
        <p>Descuento  <strong>−{formatMoney(summary.discountTotal)}</strong></p>
        <p>IVA        <strong>{formatMoney(summary.taxTotal)}</strong></p>
        <h3>Total     <strong>{formatMoney(summary.grandTotal)}</strong></h3>
      </div>
    </section>
  );
}
