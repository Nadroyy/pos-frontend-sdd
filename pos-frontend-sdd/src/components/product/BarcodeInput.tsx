import { useEffect, useRef, useState } from 'react';

type Props = {
  onScan: (barcode: string) => boolean;
  inputRef?: React.RefObject<HTMLInputElement>;
};

export function BarcodeInput({ onScan, inputRef }: Props) {
  const localRef = useRef<HTMLInputElement>(null);
  const ref = inputRef ?? localRef;

  const [barcode, setBarcode] = useState('');
  const [error, setError]     = useState('');
  const [success, setSuccess] = useState('');

  // Autofoco al montar
  useEffect(() => { ref.current?.focus(); }, []);

  function submit() {
    const code = barcode.trim();
    if (!code) return;
    const ok = onScan(code);
    if (ok) {
      setBarcode('');
      setError('');
      setSuccess('Producto agregado ✓');
      setTimeout(() => setSuccess(''), 1800);
      ref.current?.focus();
    } else {
      setError('Código de barras no encontrado.');
      setSuccess('');
    }
  }

  function handleKey(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === 'Enter') { e.preventDefault(); submit(); }
    if (e.key === 'Escape') { setBarcode(''); setError(''); setSuccess(''); }
  }

  return (
    <section className='panel barcode-panel'>
      <h2>Código de barras <kbd>F2</kbd></h2>
      <label>
        Escanear o escribir código
        <input
          ref={ref}
          value={barcode}
          onChange={e => { setBarcode(e.target.value); setError(''); setSuccess(''); }}
          onKeyDown={handleKey}
          placeholder='Ej: 200001 — Enter para agregar'
          autoComplete='off'
        />
      </label>
      <button onClick={submit}>Agregar al carrito</button>
      {success && <p className='msg-success'>{success}</p>}
      {error   && <p className='error'>{error}</p>}
    </section>
  );
}
