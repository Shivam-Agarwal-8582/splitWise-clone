import React, { useState } from 'react';
import { Modal } from '../ui/Modal';
import { Button } from '../ui/Button';
import { apiClient } from '../../lib/api';
import { Group, User } from '../../types';
import { useAuthStore } from '../../store/authStore';
import { inrToUsd, getCurrencySymbol } from '../../utils/currencyUtils';
import { ArrowRight, CheckCircle2 } from 'lucide-react';

interface SettleUpModalProps {
  isOpen: boolean;
  onClose: () => void;
  group: Group;
  defaultDebtor?: User;
  defaultCreditor?: User;
  defaultAmount?: number;
  onSettled: () => void;
}

export const SettleUpModal: React.FC<SettleUpModalProps> = ({
  isOpen,
  onClose,
  group,
  defaultDebtor,
  defaultCreditor,
  defaultAmount,
  onSettled,
}) => {
  const currentUser = useAuthStore((s) => s.user);

  const [payerId, setPayerId] = useState<number>(defaultDebtor?.id || currentUser?.id || 0);
  const [payeeId, setPayeeId] = useState<number>(defaultCreditor?.id || 0);
  const [amount, setAmount] = useState<string>(defaultAmount ? defaultAmount.toFixed(2) : '');
  const [notes, setNotes] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  React.useEffect(() => {
    if (isOpen) {
      if (defaultDebtor) setPayerId(defaultDebtor.id);
      else if (currentUser) setPayerId(currentUser.id);

      if (defaultCreditor) setPayeeId(defaultCreditor.id);
      else {
        const other = group?.members.find((m) => m.user.id !== (defaultDebtor?.id || currentUser?.id));
        if (other) setPayeeId(other.user.id);
      }

      if (defaultAmount) setAmount(defaultAmount.toFixed(2));
      setNotes('');
      setError(null);
    }
  }, [isOpen, defaultDebtor, defaultCreditor, defaultAmount, group, currentUser]);

  const parsedAmount = parseFloat(amount) || 0;
  const isINR = group?.currency?.toUpperCase() === 'INR';
  const usdEquiv = isINR && parsedAmount > 0 ? inrToUsd(parsedAmount).toFixed(2) : null;
  const currencySymbol = getCurrencySymbol(group?.currency || 'USD');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (parsedAmount <= 0) {
      setError('Please enter a valid settlement amount');
      return;
    }
    if (payerId === payeeId) {
      setError('Payer and recipient cannot be the same person');
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      await apiClient.post(`/groups/${group.id}/settlements`, {
        payerId,
        payeeId,
        amount: parsedAmount,
        currency: group.currency,
        notes: notes.trim() || undefined,
      });

      onSettled();
      onClose();
    } catch (err: any) {
      setError(err?.message || 'Failed to record settlement');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Record a Settlement" description="Record a payment made between group members">
      {error && (
        <div className="p-3 mb-4 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900 text-rose-600 dark:text-rose-400 text-xs font-medium">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="flex items-center justify-between p-4 bg-slate-50 dark:bg-slate-800/50 rounded-2xl border border-slate-200/80 dark:border-slate-800 gap-3">
          <div className="flex-1">
            <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase mb-1">Payer</label>
            <select
              value={payerId}
              onChange={(e) => setPayerId(Number(e.target.value))}
              className="w-full bg-white dark:bg-slate-900 px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm font-semibold outline-none focus:border-brand-500"
            >
              {group?.members.map((m) => (
                <option key={m.user.id} value={m.user.id}>
                  {m.user.fullName} {m.user.id === currentUser?.id ? '(You)' : ''}
                </option>
              ))}
            </select>
          </div>

          <div className="pt-5 text-slate-400">
            <ArrowRight className="w-5 h-5" />
          </div>

          <div className="flex-1">
            <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase mb-1">Recipient</label>
            <select
              value={payeeId}
              onChange={(e) => setPayeeId(Number(e.target.value))}
              className="w-full bg-white dark:bg-slate-900 px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-800 text-slate-900 dark:text-slate-100 text-sm font-semibold outline-none focus:border-brand-500"
            >
              {group?.members.map((m) => (
                <option key={m.user.id} value={m.user.id}>
                  {m.user.fullName} {m.user.id === currentUser?.id ? '(You)' : ''}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">
            Amount ({group.currency})
          </label>
          <div className="relative">
            <span className="absolute left-3.5 top-2.5 text-slate-400 font-bold text-sm">
              {currencySymbol}
            </span>
            <input
              type="number"
              step="0.01"
              min="0.01"
              required
              placeholder="0.00"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              className="w-full pl-8 pr-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 font-bold text-slate-900 dark:text-slate-100 text-sm outline-none focus:border-brand-500"
            />
          </div>
          {usdEquiv && (
            <p className="text-[11px] text-emerald-600 dark:text-emerald-400 font-semibold mt-1">
              ≈ ${usdEquiv} USD dollar equivalent
            </p>
          )}
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">
            Notes (Optional)
          </label>
          <input
            type="text"
            placeholder="payment_reference_or_notes"
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 text-sm outline-none focus:border-brand-500"
          />
        </div>

        <div className="flex justify-end gap-3 pt-4 border-t border-slate-100 dark:border-slate-800 mt-6">
          <Button type="button" variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" isLoading={isLoading}>
            <CheckCircle2 className="w-4 h-4 mr-1" />
            Record Payment
          </Button>
        </div>
      </form>
    </Modal>
  );
};
