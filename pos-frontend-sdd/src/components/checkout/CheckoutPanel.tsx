import { useMemo, useState } from 'react';
import type { Payment, PaymentMethod } from '../../types/payment';
import type { CartItem, CartSummary } from '../../types/cart';
import type { Receipt } from '../../types/receipt';
import { formatMoney, roundMoney } from '../../utils/money';
import { registrarVenta } from '../../services/ventaService';

type Props = {
  items: CartItem[];
  summary: CartSummary;
  onComplete: (r: Receipt) => void;
  amountRef?: React.RefObject<HTMLInputElement | null>;
};

export function CheckoutPanel({ items, summary, onComplete, amountRef }: Props) {
  const [method,     setMethod]     = useState<PaymentMethod>('cash');
  const [amount,     setAmount]     = useState('');
  const [reference,  setReference]  = useState('');
  const [payments,   setPayments]   = useState<Payment[]>([]);
  const [completing, setCompleting] = useState(false);
  const [error,      setError]      = useState<string | null>(null);

  const paid      = useMemo(() => roundMoney(payments.reduce((s, p) => s + p.amount, 0)), [payments]);
  const remaining = roundMoney(Math.max(summary.grandTotal - paid, 0));
  const change    = roundMoney(Math.max(paid - summary.grandTotal, 0));
  const canCheckout = items.length > 0 && paid >= summary.grandTotal;

  function addPayment() {
    const a = Number(amount);
    if (a <= 0) return;
    setPayments(cur => [...cur, { id: String(Date.now() + Math.random()), method, amount: a, reference }]);
    setAmount('');
    setReference('');
  }

  async function complete() {
    if (!canCheckout || completing) return;
    setCompleting(true);
    setError(null);

    try {
      const venta = await registrarVenta(items, summary.grandTotal);

      onComplete({
        receiptNumber: venta.saleId,
        createdAt:     venta.timestamp,
        items,
        summary,
        payments,
        change,
      });

      setPayments([]);
    } catch {
      setError('No se pudo registrar la venta. Verifica la conexión e intenta de nuevo.');
    } finally {
      setCompleting(false);
    }
  }

  return (
    <div className='co-wrap'>

      {/* Cobro F4 + total */}
      <div className='co-head'>
        <span className='co-label'>Cobro F4</span>
        <strong className='co-total'>{formatMoney(summary.grandTotal)}</strong>
      </div>

      {/* Fila de pago */}
      <div className='co-pay-row'>
        <select
          className='co-sel'
          value={method}
          onChange={e => setMethod(e.target.value as PaymentMethod)}
          tabIndex={0}
        >
          <option value='cash'>Efectivo</option>
          <option value='card'>Tarjeta</option>
          <option value='wallet'>Billetera</option>
        </select>
        <input
          ref={amountRef}
          className='co-amt'
          type='number'
          min='0'
          placeholder='Monto'
          value={amount}
          onChange={e => setAmount(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && addPayment()}
          tabIndex={0}
        />
        {method !== 'cash' && (
          <input
            className='co-ref'
            placeholder='Referencia'
            value={reference}
            onChange={e => setReference(e.target.value)}
            tabIndex={0}
          />
        )}
        <button className='co-add-btn' onClick={addPayment} tabIndex={0}>+ Pago</button>
      </div>

      {/* Pagos registrados */}
      {payments.length > 0 && (
        <div className='co-chips'>
          {payments.map(p => (
            <span key={p.id} className='co-chip'>
              {p.method}: {formatMoney(p.amount)}{p.reference ? ` (${p.reference})` : ''}
            </span>
          ))}
        </div>
      )}

      {/* Pagado / Restante / Cambio */}
      <div className='co-status'>
        <span>Pagado: <strong>{formatMoney(paid)}</strong></span>
        <span>Restante: <strong>{formatMoney(remaining)}</strong></span>
        <span>Cambio: <strong>{formatMoney(change)}</strong></span>
      </div>

      {/* Error de API */}
      {error && (
        <div className='fb-err'>{error}</div>
      )}

      {/* Completar */}
      <button
        className='co-complete'
        disabled={!canCheckout || completing}
        onClick={complete}
        tabIndex={0}
      >
        {completing ? 'Registrando...' : 'Completar Venta'}
      </button>

    </div>
  );
}
