import React from 'react';
import { formatDualCurrency } from '../../utils/currencyUtils';

interface MoneyDisplayProps {
  amount: number;
  currency?: string;
  className?: string;
  secondaryClassName?: string;
  showSign?: boolean;
}

export const MoneyDisplay: React.FC<MoneyDisplayProps> = ({
  amount,
  currency = 'USD',
  className = '',
  secondaryClassName = '',
  showSign = false,
}) => {
  const { primary, secondary } = formatDualCurrency(amount, currency);

  const sign = showSign && amount > 0.005 ? '+' : '';

  return (
    <span className={`inline-flex items-baseline gap-1.5 flex-wrap ${className}`}>
      <span>
        {sign}
        {primary}
      </span>
      {secondary && (
        <span
          className={`text-[0.78em] font-medium opacity-75 tracking-tight ${secondaryClassName}`}
          title="Converted at current USD/INR rate"
        >
          ({secondary})
        </span>
      )}
    </span>
  );
};

