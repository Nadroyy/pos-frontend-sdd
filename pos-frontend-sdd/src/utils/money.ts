export function formatMoney(value: number): string { return new Intl.NumberFormat('en-US', { style:'currency', currency:'USD' }).format(value); }
export function roundMoney(value: number): number { return Math.round(value * 100) / 100; }
