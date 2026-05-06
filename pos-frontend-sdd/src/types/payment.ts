export type PaymentMethod = 'cash' | 'card' | 'wallet';
export type Payment = { id: string; method: PaymentMethod; amount: number; reference?: string; };
