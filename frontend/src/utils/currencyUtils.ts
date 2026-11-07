// Currency utilities with dual INR & USD display support
export const USD_TO_INR_RATE = 84.0;

export const CURRENCY_SYMBOLS: Record<string, string> = {
  INR: '₹',
  USD: '$',
  EUR: '€',
  GBP: '£',
  CAD: 'CA$',
  AUD: 'AU$',
};

export function getCurrencySymbol(currency: string = 'USD'): string {
  return CURRENCY_SYMBOLS[currency.toUpperCase()] || currency;
}

export function inrToUsd(inrAmount: number): number {
  return inrAmount / USD_TO_INR_RATE;
}

export function usdToInr(usdAmount: number): number {
  return usdAmount * USD_TO_INR_RATE;
}

export function formatMoney(amount: number, currency: string = 'USD'): string {
  const symbol = getCurrencySymbol(currency);
  const formatted = Math.abs(amount).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
  const sign = amount < 0 ? '-' : '';
  return `${sign}${symbol}${formatted}`;
}

export function formatDualCurrency(amount: number, currency: string = 'USD'): {
  primary: string;
  secondary: string | null;
} {
  const primary = formatMoney(amount, currency);

  if (currency.toUpperCase() === 'INR') {
    const usdAmount = inrToUsd(amount);
    const secondary = formatMoney(usdAmount, 'USD');
    return { primary, secondary: `~${secondary}` };
  }

  return { primary, secondary: null };
}

