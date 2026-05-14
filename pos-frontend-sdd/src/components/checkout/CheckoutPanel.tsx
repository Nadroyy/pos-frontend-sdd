import { useMemo, useRef, useState } from 'react';
import type { Payment, PaymentMethod } from '../../types/payment';
import type { CartItem, CartSummary } from '../../types/cart';
import type { Receipt } from '../../types/receipt';
import { formatMoney, roundMoney } from '../../utils/money';

type Props = {
  items: CartItem[];
  summary: CartSummary;
  onComplete: (r: Receipt) => void;
  checkoutRef?: React.RefObject<HTMLElement>;
};

export function CheckoutPanel({ items, summary, onComplete, checkoutRef }: Props) {
  const [method, setMethod]       = useState<PaymentMethod>('cash');
  const [amount, setAmount]       = useState('');
  const [reference, setReference] = useState('');
  const [payments, setPayments]   = useState<Payment[]>([]);

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

  function complete() {
    if (!canCheckout) return;
    onComplete({
      receiptNumber: `POS-${Date.now()}`,
      createdAt: new Date().toLocaleString(),
      items, summary, payments, change,
    });
    setPayments([]);
  }

  return (
    <section className='panel' ref={checkoutRef as React.RefObject<HTMLElement>}>
      <h2>Cobro <kbd>F4</kbd></h2>
      <p className='total-due'>Monto a pagar: <strong>{formatMoney(summary.grandTotal)}</strong></p>

      <div className='grid two'>
        <label>
          Método
          <select value={method} onChange={e => setMethod(e.target.value as PaymentMethod)}>
            <option value='cash'>Efectivo</option>
            <option value='card'>Tarjeta</option>
            <option value='wallet'>Billetera Digital</option>
          </select>
        </label>
        <label>
          Monto
          <input
            type='number' min='0'
            value={amount}
            onChange={e => setAmount(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && addPayment()}
          />
        </label>
      </div>

      {method !== 'cash' && (
        <label>
          Referencia
          <input
            value={reference}
            onChange={e => setReference(e.target.value)}
            placeholder='Referencia de transacción'
          />
        </label>
      )}

      <button onClick={addPayment}>Agregar Pago</button>

      <div>
        {payments.map(p => (
          <p key={p.id}>{p.method}: {formatMoney(p.amount)} {p.reference && `(${p.reference})`}</p>
        ))}
      </div>

      <p>Pagado:    <strong>{formatMoney(paid)}</strong></p>
      <p>Restante:  <strong>{formatMoney(remaining)}</strong></p>
      <p>Cambio:    <strong>{formatMoney(change)}</strong></p>

      {!canCheckout && (
        <p className='muted'>Agrega el pago suficiente para completar la venta.</p>
      )}

      <button disabled={!canCheckout} onClick={complete} className='btn-complete'>
        Completar Venta
      </button>
    </section>
  );
}
