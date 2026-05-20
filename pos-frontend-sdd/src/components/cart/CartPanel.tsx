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
    if (window.confirm('¿Vaciar el carrito?')) onClear();
  }

  const hasDiscount = discount.type !== 'none' && discount.value > 0;

  return (
    <div className='cart-wrap'>

      {/* Encabezado del carrito */}
      <div className='cart-head-row'>
        <span>Carrito ({items.length})</span>
        {items.length > 0 && (
          <button className='lnk' onClick={handleClear} tabIndex={0}>Vaciar</button>
        )}
      </div>

      {/* Líneas de producto */}
      {items.length === 0
        ? <div className='cart-empty'>
            <div>No hay productos en el carrito.</div>
            <div className='cart-empty-hint'>Escanea un codigo o busca un producto para comenzar.</div>
          </div>
        : items.map(i => (
          <div key={i.product.id} className='cart-line'>
            <span className='cl-name'>
              {i.product.name}
              {i.product.taxRate > 0 && <span className='cl-iva'> IVA{(i.product.taxRate*100).toFixed(0)}%</span>}
            </span>
            <span className='cl-detail'>
              ({i.quantity}) x {formatMoney(i.product.price)} = {formatMoney(i.product.price * i.quantity)}
            </span>
            <span className='cl-actions'>
              <button className='qty-btn' onClick={() => onQuantity(i.product.id, i.quantity - 1)} tabIndex={0}>-</button>
              <button className='qty-btn' onClick={() => onQuantity(i.product.id, i.quantity + 1)} tabIndex={0}>+</button>
              <button className='del-btn' onClick={() => onRemove(i.product.id)} tabIndex={0}>x</button>
            </span>
          </div>
        ))
      }

      {/* Descuento */}
      <div className='disc-row'>
        <select
          className='disc-sel'
          value={discount.type}
          onChange={e => onDiscount({ type: e.target.value as Discount['type'], value: discount.value })}
          tabIndex={0}
        >
          <option value='none'>Sin descuento</option>
          <option value='percentage'>% Descuento</option>
          <option value='fixed'>$ Descuento</option>
        </select>
        {discount.type !== 'none' && (
          <>
            <input
              className='disc-val'
              type='number' min='0'
              value={discount.value}
              onChange={e => onDiscount({ ...discount, value: Number(e.target.value) })}
              tabIndex={0}
            />
            <button className='lnk' onClick={() => onDiscount({ type: 'none', value: 0 })} tabIndex={0}>quitar</button>
          </>
        )}
      </div>

      {/* Totales */}
      <div className='totals-wrap'>
        {hasDiscount && (
          <div className='total-line'>
            <span>Descuento</span><span>-{formatMoney(summary.discountTotal)}</span>
          </div>
        )}
        <div className='total-line'>
          <span>IVA</span><span>{formatMoney(summary.taxTotal)}</span>
        </div>
        <div className='total-line total-final'>
          <span>Total</span><strong>{formatMoney(summary.grandTotal)}</strong>
        </div>
      </div>

    </div>
  );
}
